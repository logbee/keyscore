import {Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DraggableModel} from "./models/draggable.model";
import {Draggable, Dropzone, Workspace} from "./models/contract";
import {DropzoneType} from "./models/dropzone-type";
import {deepcopy} from "../../../util";
import {DropzoneFactory} from "./dropzone/dropzone-factory";
import {DraggableFactory} from "./draggable/draggable-factory";
import {computeRelativePositionToParent} from "./util/util";

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
    @ViewChild("workspace", {read: ViewContainerRef}) mirrorContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ElementRef}) workspaceElement: ElementRef;

    public id: string;

    public dropzones: Set<Dropzone> = new Set();

    public toolbar: Dropzone;

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
        console.log("Dragged: ", this.dragged.getDraggableModel());
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

    @HostListener('document:mouseup', ['$event'])
    dragStop(event: MouseEvent) {
        if (this.isDragging) {
            if (this.bestDropzone) {
                this.bestDropzone.drop(this.mirror, this.dragged);
            }
            if (!this.dragged.isVisible()) {
                this.dragged.show();
            }
            this.mirror.destroy();
            this.isDragging = false;
        }
        this.bestDropzone = null;
    }

    private initialiseMirrorComponent(): DraggableModel {
        const workspaceRect = this.workspaceElement.nativeElement.getBoundingClientRect();
        const relativeMirrorPosition = computeRelativePositionToParent(
            this.dragged.getAbsoluteDraggablePosition(),
            {x: workspaceRect.left, y: workspaceRect.top});

        return {
            ...this.dragged.getDraggableModel(),
            isMirror: true,
            position: relativeMirrorPosition
        };
    }


    private createAndRegisterDraggable(container: ViewContainerRef, model: DraggableModel) {
        this.registerDraggable(this.draggableFactory.createDraggable(container, model, this));
    }

    private createAndRegisterMirror(model: DraggableModel) {
        this.mirror = this.draggableFactory.createDraggable(this.mirrorContainer, model, this);
        console.log("Mirror: ", this.mirror.getDraggableModel());
        this.registerMirror(this.mirror);

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

    registerMirror(mirror: Draggable) {
        mirror.dragMove$.subscribe(() => this.dragMove(mirror));
    }

    registerDraggable(draggable: Draggable) {
        draggable.dragStart$.subscribe(() => this.dragStart(draggable));
    }

    ngOnInit() {
        this.toolbar = this.dropzoneFactory.createToolbarDropzone(this.toolbarContainer, this);

        this.dropzones.add(this.dropzoneFactory.createWorkspaceDropzone(this.workspaceContainer, this));

        for (let i = 0; i < 2; i++) {
            this.createAndRegisterDraggable(this.toolbar.getDraggableContainer(), {
                name: "Test" + Math.random().toString().substr(0,4),
                draggableType: "general",
                nextConnection: {isPermitted: true, connectableTypes: ["general"]},
                previousConnection: {isPermitted: true, connectableTypes: ["general"]},
                initialDropzone: this.toolbar,
                next: null,
                rootDropzone: this.toolbar.getDropzoneModel().dropzoneType,
                isMirror: false
            });
        }
    }

    ngOnDestroy() {

    }

}
