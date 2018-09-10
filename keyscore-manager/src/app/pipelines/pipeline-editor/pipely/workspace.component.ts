import {
    AfterViewInit, Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild,
    ViewContainerRef
} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DraggableModel} from "./models/draggable.model";
import {Draggable, Dropzone, Workspace} from "./models/contract";
import {DropzoneType} from "./models/dropzone-type";
import {DropzoneFactory} from "./dropzone/dropzone-factory";
import {DraggableFactory} from "./draggable/draggable-factory";
import {computeRelativePositionToParent} from "./util/util";
import {WorkspaceDropzoneSubcomponent} from "./dropzone/workspace-dropzone-subcomponent";

@Component({
    selector: "workspace",
    template: `
        <div #workspace class="workspace">
            <div class="row">
                <ng-template #toolbarContainer></ng-template>
                <ng-template #workspaceContainer>
                </ng-template>
            </div>
        </div>
    `
})


export class WorkspaceComponent implements OnInit, OnDestroy, Workspace, AfterViewInit {
    @ViewChild("workspaceContainer", {read: ViewContainerRef}) workspaceContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ViewContainerRef}) mirrorContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ElementRef}) workspaceElement: ElementRef;
    @ViewChild("toolbarContainer", {read: ViewContainerRef}) toolbarContainer: ViewContainerRef;


    public id: string;

    public dropzones: Set<Dropzone> = new Set();
    public draggables: Draggable[] = [];

    public toolbarDropzone: Dropzone;
    public workspaceDropzone: Dropzone;

    private isDragging: boolean = false;
    private bestDropzone: Dropzone;
    private dragged: Draggable;
    private mirror: Draggable;

    constructor(private dropzoneFactory: DropzoneFactory,
                private draggableFactory: DraggableFactory) {
        this.id = uuid();
    }


    private dragStart(draggable: Draggable) {
        this.dragged = draggable;
        this.isDragging = true;

        const mirrorModel = this.initialiseMirrorComponent();
        this.createAndRegisterMirror(mirrorModel);

        if (this.dragged.getDraggableModel().rootDropzone === DropzoneType.Workspace) {
            this.dragged.hide();
        }
    }

    private dragMove(mirror: Draggable) {
        let lastDropzone: Dropzone = null;
        this.dropzones.forEach(dropzone => {
            lastDropzone = dropzone.computeBestDropzone(mirror, lastDropzone);
        });
        if (this.bestDropzone) {
            this.bestDropzone.setIsDroppable(false);
        }
        if (lastDropzone) {
            lastDropzone.setIsDroppable(true);
        }
        this.bestDropzone = lastDropzone;
    }

    @HostListener('mouseup', ['$event'])
    dragStop(event: MouseEvent) {
        if (this.isDragging) {
            if (this.bestDropzone) {
                this.bestDropzone.drop(this.mirror, this.dragged);
            }
            if (this.dragged && !this.dragged.isVisible()) {
                this.dragged.show();
            }

            this.mirror.destroy();

            this.isDragging = false;
        }
        this.bestDropzone = null;
    }

    private initialiseMirrorComponent(): DraggableModel {
        const workspaceRect = this.workspaceElement.nativeElement.getBoundingClientRect();
        const scrollContainer: ElementRef =
            (this.workspaceDropzone.getSubComponent() as WorkspaceDropzoneSubcomponent)
                .workspaceScrollContainer;
        let draggedPos = this.dragged.getAbsoluteDraggablePosition();

        const absolutePos = {x: draggedPos.x + scrollContainer.nativeElement.scrollLeft, y: draggedPos.y};
        const relativeMirrorPosition = computeRelativePositionToParent(
            absolutePos,
            {x: workspaceRect.left, y: workspaceRect.top});

        return {
            ...this.dragged.getDraggableModel(),
            isMirror: true,
            position: relativeMirrorPosition,
            previous:null
        };
    }


    private createAndRegisterMirror(model: DraggableModel) {
        this.mirror = this.draggableFactory.createDraggable(this.workspaceDropzone.getDraggableContainer(), model, this);
        this.registerMirror(this.mirror);

    }

    private computeWorkspaceSize() {
        const compResult =
            (this.workspaceDropzone.getSubComponent() as WorkspaceDropzoneSubcomponent)
                .resizeWorkspaceOnDrop(this.draggables);

        this.draggables.forEach(draggable => draggable.moveXAxis(compResult));
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

    removeDraggables(predicate: (draggable: Draggable) => boolean) {
        this.draggables.forEach((draggable, index, array) => {
            if (predicate(draggable)) {
                array.splice(index, 1);
            }
        });
    }

    registerMirror(mirror: Draggable) {
        mirror.dragMove$.subscribe(() => this.dragMove(mirror));
    }

    registerDraggable(draggable: Draggable) {
        draggable.dragStart$.subscribe(() => this.dragStart(draggable));
        if (draggable.getDraggableModel().initialDropzone
                .getDropzoneModel().dropzoneType === DropzoneType.Workspace) {
            this.draggables.push(draggable);
        }
        if (draggable.getDraggableModel().initialDropzone.getDropzoneModel().dropzoneType !==
            DropzoneType.Toolbar) {
            this.computeWorkspaceSize();
        }
    }

    getWorkspaceDropzone(): Dropzone {
        return this.workspaceDropzone;
    }

    ngOnInit() {
        this.workspaceDropzone = this.dropzoneFactory.createWorkspaceDropzone(this.workspaceContainer, this);
        this.toolbarDropzone = this.dropzoneFactory.createToolbarDropzone(this.toolbarContainer, this);

        this.dropzones.add(this.workspaceDropzone);
        this.dropzones.add(this.dropzoneFactory.createTrashDropzone(this.workspaceContainer, this));

        let inType:string="no-connection-in";
        let outType:string="default-out";
        for (let i = 0; i < 3; i++) {

            if(i===1){
                inType = "default-in";
            }else if(i==2){
                outType ="no-connection-out"
            }
            this.draggableFactory.createDraggable(this.toolbarDropzone.getDraggableContainer(), {
                name: "Test" + Math.random().toString().substr(0, 4),
                draggableType: "default",
                previousConnection: {isPermitted: true, connectableTypes: inType !== "no-connection-in" ? ["default-out"]:[],connectionType:inType},
                nextConnection: {isPermitted: true, connectableTypes: outType !== "no-connection-out" ? ["default-in"]:[],connectionType:outType},
                initialDropzone: this.toolbarDropzone,
                next: null,
                previous:null,
                rootDropzone: this.toolbarDropzone.getDropzoneModel().dropzoneType,
                isMirror: false
            }, this);
        }

    }

    ngAfterViewInit() {

    }

    ngOnDestroy() {

    }

}
