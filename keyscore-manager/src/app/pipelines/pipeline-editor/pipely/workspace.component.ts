import {Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DraggableModel} from "./models/draggable.model";
import {Draggable, Dropzone, Workspace} from "./models/contract";
import {DropzoneType} from "./models/dropzone-type";
import {deepcopy} from "../../../util";
import {DropzoneFactory} from "./dropzone/dropzone-factory";
import {DraggableFactory} from "./draggable/draggable-factory";
import {switchMap, takeUntil} from "rxjs/operators";
import {mergeMap} from "rxjs/internal/operators";
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
    @ViewChild("workspace", {read: ViewContainerRef}) wrapperContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ElementRef}) workspaceElement: ElementRef;

    public id: string;

    public dropzones: Set<Dropzone> = new Set();

    public toolbar: Dropzone;

    private isDragging: boolean = false;
    private bestDropzone: Dropzone;
    private currentDragged: Draggable;
    private currentMirror: Draggable;

    constructor(private dropzoneFactory: DropzoneFactory,
                private draggableFactory: DraggableFactory) {
        this.id = uuid();
    }


    private dragStart(draggable: Draggable) {
        this.currentDragged = draggable;
        this.isDragging = true;

        const mirrorModel = this.initialiseMirrorComponent();
        this.createAndRegisterMirror(mirrorModel);

        if (this.currentDragged.getDraggableModel().rootDropzone === DropzoneType.Workspace) {
            this.currentDragged.hide();
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
                this.bestDropzone.drop(this.currentMirror,this.currentDragged);
            }
            if (!this.currentDragged.isVisible()) {
                this.currentDragged.show();
            }
            this.currentMirror.destroy();
            this.isDragging = false;
            this.currentMirror = undefined;
        }
    }

    private initialiseMirrorComponent(): DraggableModel {
        const workspaceRect = this.workspaceElement.nativeElement.getBoundingClientRect();
        const relativeMirrorPosition = computeRelativePositionToParent(
            this.currentDragged.getAbsoluteDraggablePosition(),
            {x: workspaceRect.left, y: workspaceRect.top});

        return {
            ...deepcopy(this.currentDragged.getDraggableModel()),
            isMirror: true,
            position: relativeMirrorPosition
        };
    }



    private createAndRegisterDraggable(container: ViewContainerRef, model: DraggableModel) {
        this.registerDraggable(this.draggableFactory.createDraggable(container, model, this));
    }

    private createAndRegisterMirror(model: DraggableModel) {
        this.currentMirror = this.draggableFactory.createDraggable(this.wrapperContainer, model, this);
        this.registerMirror(this.currentMirror);
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
        this.toolbar = this.dropzoneFactory.createToolbarDropzone(this.toolbarContainer,this);

        this.dropzones.add(this.dropzoneFactory.createWorkspaceDropzone(this.workspaceContainer,this));

        for (let i = 0; i < 2; i++) {
            this.createAndRegisterDraggable(this.toolbar.getDraggableContainer(), {
                name: "Test" + Math.random(),
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
