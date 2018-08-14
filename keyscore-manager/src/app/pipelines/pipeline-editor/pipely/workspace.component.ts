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
import {DropzoneType} from "./models/dropzone-type";
import {deepcopy} from "../../../util";
import {DropzoneFactory} from "./dropzone/dropzone-factory";

@Component({
    selector: "workspace",
    template: `
        <div #workspace class="workspace col-12">
            <div class="row">
                <ng-template #toolbarContainer></ng-template>
            </div>
            <div class="row">
                <ng-template #workspaceContainer></ng-template>
            </div>
        </div>
    `
})


export class WorkspaceComponent implements OnInit, OnDestroy, Workspace {
    @ViewChild("toolbarContainer", {read: ViewContainerRef}) toolbarContainer: ViewContainerRef;
    @ViewChild("workspaceContainer", {read: ViewContainerRef}) workspaceContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ElementRef}) workspaceElement: ElementRef;

    public id: string;

    public dropzones: Set<Dropzone> = new Set();
    public draggables: Draggable[] = [];

    public toolbar: Dropzone;

    private isDragging: boolean = false;
    private bestDropzone: Dropzone;
    private currentDragged: Draggable;
    private currentMirror: Draggable;

    constructor(private resolver: ComponentFactoryResolver, private dropzoneFactory: DropzoneFactory) {
        this.id = uuid();
    }

    private dragStart(draggableRef: ComponentRef<DraggableComponent>) {
        this.currentDragged = draggableRef.instance;
        this.isDragging = true;

        const mirrorModel = this.initialiseMirrorComponent();
        this.createMirrorComponent(mirrorModel);

        if (this.currentDragged.getDraggableModel().rootDropzone === DropzoneType.Workspace) {
            this.currentDragged.hide();
        }
    }

    private dragMove(mirrorRef: ComponentRef<DraggableComponent>) {
        let lastDropzone: Dropzone = null;
        this.dropzones.forEach(dropzone => {
            lastDropzone = dropzone.computeBestDropzone(mirrorRef.instance, lastDropzone);
        });
        if (this.bestDropzone) {
            this.bestDropzone.setIsDroppable(false);
        }
        if (lastDropzone) {
            lastDropzone.setIsDroppable(true);
        }
        this.bestDropzone = lastDropzone;
    }

    @HostListener('document:mouseup', ['$event'])
    dragStop(event: MouseEvent) {
        if (this.isDragging) {
            if (this.bestDropzone) {
                this.drop(this.bestDropzone);
            }
            else if (!this.bestDropzone && !this.currentDragged.isVisible()) {
                this.currentDragged.show();
            }
            this.currentMirror.destroy();
            this.isDragging = false;
            this.currentMirror = undefined;
        }
    }


    private drop(targetDropzone) {
        targetDropzone.setIsDroppable(false);
        let draggableModel: DraggableModel;
        switch (targetDropzone.getDropzoneModel().dropzoneType) {
            case DropzoneType.Workspace:
                draggableModel = {
                    ...deepcopy(this.currentMirror.getDraggableModel()),
                    initialDropzone: targetDropzone,
                    rootDropzone: DropzoneType.Workspace,
                    isMirror: false,
                    position: this.computeRelativePositionToParent(this.currentMirror.getAbsoluteDraggablePosition(),
                        targetDropzone.getAbsolutePosition()),
                    parent: null

                };
                break;
            case DropzoneType.Connector:
                draggableModel = {
                    ...deepcopy(this.currentDragged.getDraggableModel()),
                    initialDropzone: targetDropzone,
                    rootDropzone: DropzoneType.Workspace,
                    hasAbsolutePosition: false,
                    parent: targetDropzone.getOwner()
                };
                targetDropzone.occupyDropzone();
                break;
        }

        const initialDropzone = this.currentDragged.getDraggableModel().initialDropzone;
        switch (initialDropzone.getDropzoneModel().dropzoneType) {
            case DropzoneType.Connector:
                initialDropzone.clearDropzone();
        }

        switch (this.currentDragged.getDraggableModel().rootDropzone) {
            case DropzoneType.Workspace:
                this.currentDragged.destroy();
                break;

        }

        this.createDraggableComponent(targetDropzone, draggableModel);

    }

    createDraggableComponent(dropzone: Dropzone, draggableModel: DraggableModel) {
        const draggableFactory = this.resolver.resolveComponentFactory(DraggableComponent);
        const draggableRef = dropzone.getDraggableContainer().createComponent(draggableFactory);
        this.draggables.push(draggableRef.instance);
        draggableRef.instance.workspace = this;
        draggableRef.instance.draggableModel = deepcopy(draggableModel);
        draggableRef.instance.componentRef = draggableRef;

        draggableRef.instance.dragStart$.subscribe(() => {
            this.dragStart(draggableRef);

        });

        return draggableRef.instance;


    }

    private createMirrorComponent(draggableModel: DraggableModel) {
        const draggableFactory = this.resolver.resolveComponentFactory(DraggableComponent);
        const mirrorRef = this.toolbarContainer.createComponent(draggableFactory);
        this.currentMirror = mirrorRef.instance;
        mirrorRef.instance.workspace = this;
        mirrorRef.instance.draggableModel = deepcopy(draggableModel);
        mirrorRef.instance.componentRef = mirrorRef;


        mirrorRef.instance.dragMove$.subscribe(() =>
            this.dragMove(mirrorRef)
        );


    }

    private initialiseMirrorComponent(): DraggableModel {
        const workspaceRect = this.workspaceElement.nativeElement.getBoundingClientRect();
        const relativeMirrorPosition = this.computeRelativePositionToParent(
            this.currentDragged.getAbsoluteDraggablePosition(),
            {x: workspaceRect.left, y: workspaceRect.top});

        return {
            ...deepcopy(this.currentDragged.getDraggableModel()),
            isMirror: true,
            hasAbsolutePosition: true,
            position: relativeMirrorPosition
        };
    }

    private computeRelativePositionToParent(absolutePosition: { x: number, y: number },
                                            absolutePositionParent: { x: number, y: number }) {
        return {
            x: (absolutePosition.x - absolutePositionParent.x),
            y: (absolutePosition.y - absolutePositionParent.y)
        }
    }

    addDropzone(dropzone: Dropzone) {
        this.dropzones.add(dropzone);
    }

    removeAllDropzones(predicate: (dropzone: Dropzone) => boolean) {
        this.dropzones.forEach(dropzone => {
            if (predicate(dropzone)) {
                this.dropzones.delete(dropzone);
            }
        });
    }

    ngOnInit() {
        this.toolbar = this.dropzoneFactory.createToolbarDropzone(this.toolbarContainer);

        this.dropzones.add(this.dropzoneFactory.createWorkspaceDropzone(this.workspaceContainer));

        for (let i = 0; i < 2; i++) {
            this.createDraggableComponent(this.toolbar, {
                name: "Test" + Math.random(),
                hasAbsolutePosition: false,
                draggableType: "general",
                nextConnection: {isPermitted: true, connectableTypes: ["general"]},
                previousConnection: {isPermitted: true, connectableTypes: ["general"]},
                initialDropzone: this.toolbar,
                parent: null,
                next: null,
                rootDropzone: this.toolbar.getDropzoneModel().dropzoneType,
                isMirror: false
            });
        }
    }


    ngOnDestroy() {

    }

}
