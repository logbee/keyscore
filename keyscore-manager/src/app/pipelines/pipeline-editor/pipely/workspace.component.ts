import {
    Component,
    ComponentFactoryResolver,
    ComponentRef,
    ElementRef,
    HostListener,
    OnDestroy,
    OnInit,
    ViewChild,
    ViewContainerRef
} from "@angular/core";
import {DropzoneComponent} from "./dropzone.component";
import {v4 as uuid} from "uuid";
import {DropzoneModel} from "./models/dropzone.model";
import {DraggableModel} from "./models/draggable.model";
import {Draggable, Dropzone, Workspace} from "./models/contract";
import {DraggableComponent} from "./draggable.component";

@Component({
    selector: "workspace",
    template: `
        <div class="workspace col-12">
            <div #workspace class="row">
                <ng-template #dropzoneContainer></ng-template>
            </div>
        </div>
    `
})


export class WorkspaceComponent implements OnInit, OnDestroy, Workspace {
    @ViewChild("dropzoneContainer", {read: ViewContainerRef}) dropzoneContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ElementRef}) workspaceElement: ElementRef;

    public id: string;

    public dropzones: Dropzone[] = [];
    public draggables: Draggable[] = [];

    private isDragged: boolean = false;
    private currentDragged: Draggable;
    private currentMirror: ComponentRef<DraggableComponent>;

    constructor(private resolver: ComponentFactoryResolver) {
        this.id = uuid();
    }

    @HostListener('document:mouseup', ['$event'])
    onMouseUp(event: MouseEvent) {
        if (this.isDragged) {
            this.dropzones.forEach(dropzone => {
                if (dropzone.getIsDroppable()) {
                    dropzone.setIsDroppable(false);
                    let draggableModel = {...this.currentDragged.getDraggableModel(), initialDropzone: dropzone};
                    this.createDraggableComponent(dropzone, draggableModel);
                }
            });
            this.currentMirror.destroy();
            this.isDragged = false;
        }
    }


    private createDropzoneComponent(dropzoneModel: DropzoneModel) {
        const dropzoneFactory = this.resolver.resolveComponentFactory(DropzoneComponent);
        const dropzoneRef = this.dropzoneContainer.createComponent(dropzoneFactory);
        this.dropzones.push(dropzoneRef.instance);
        dropzoneRef.instance.workspace = this;
        dropzoneRef.instance.dropzoneModel = dropzoneModel;

        console.log("Dropzone created");

    }

    private createMirrorComponent(draggableModel: DraggableModel) {
        const draggableFactory = this.resolver.resolveComponentFactory(DraggableComponent);
        const mirrorRef = this.dropzoneContainer.createComponent(draggableFactory);
        this.currentMirror = mirrorRef;
        mirrorRef.instance.workspace = this;
        mirrorRef.instance.draggableModel = draggableModel;

        mirrorRef.instance.dragMove$.subscribe(() =>
            this.dropzones.filter(dropzone =>
                dropzone.getId() !== mirrorRef.instance.getDraggableModel().initialDropzone.getId())
                .forEach(dropzone => dropzone.isDraggableInRange(mirrorRef.instance))
        );
        console.log("Mirror created");


    }

    private createDraggableComponent(dropzone: Dropzone, draggableModel: DraggableModel) {
        const draggableFactory = this.resolver.resolveComponentFactory(DraggableComponent);
        const draggableRef = dropzone.getDraggableContainer().createComponent(draggableFactory);
        this.draggables.push(draggableRef.instance);
        draggableRef.instance.workspace = this;
        draggableRef.instance.draggableModel = draggableModel;

        draggableRef.instance.dragStart$.subscribe(() => {
            this.currentDragged = draggableRef.instance;
            this.isDragged = true;

            this.initialiseMirrorComponent();
        });
        console.log("Draggable created");


    }

    private initialiseMirrorComponent() {
        const relativeMirrorPosition = this.computeRelativePositionToWorkspace(
            this.currentDragged.getAbsoluteDraggablePosition());

        let mirrorModel = {
            ...this.currentDragged.getDraggableModel(),
            isMirror: true,
            hasAbsolutePosition: true,
            position: relativeMirrorPosition
        };
        this.createMirrorComponent(mirrorModel);
    }

    private computeRelativePositionToWorkspace(absolutePosition: { x: number, y: number }) {
        const workspaceRect = this.workspaceElement.nativeElement.getBoundingClientRect();
        return {
            x: (absolutePosition.x - workspaceRect.left),
            y: (absolutePosition.y - workspaceRect.top)
        }
    }

    public ngOnInit() {
        this.createDropzoneComponent(
            {dropzoneType: "toolbar", dropzoneRadius: 0, acceptedDraggableTypes: []}
        );
        this.createDropzoneComponent(
            {dropzoneType: "workspace", dropzoneRadius: 0, acceptedDraggableTypes: ["general"]}
        );

        this.dropzones.forEach(dropzone => {
            for (let i = 0; i < 2; i++) {
                this.createDraggableComponent(dropzone, {
                    name: "Test" + Math.random(),
                    hasAbsolutePosition: false,
                    draggableType: "general",
                    initialDropzone: dropzone,
                    isMirror: false
                });
            }
        })
    }

    public ngOnDestroy() {

    }

}
