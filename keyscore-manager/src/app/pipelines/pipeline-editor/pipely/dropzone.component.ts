import {Component, ElementRef, HostBinding, Input, OnDestroy, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DropzoneModel} from "./models/dropzone.model";
import {Draggable, Dropzone, Workspace} from "./models/contract";
import {Rectangle} from "./models/rectangle";
import {computeDistance, intersects} from "./util/util";
import {DropzoneType} from "./models/dropzone-type";

@Component({
    selector: "dropzone",
    template: `
        <div #dropzone [class.is-droppable]="isDroppable">
            <ng-template #draggableContainer></ng-template>
        </div>
    `
})


export class DropzoneComponent implements OnInit, OnDestroy, Dropzone {

    workspace: Workspace;
    dropzoneModel: DropzoneModel;
    owner: Draggable;

    @HostBinding('class.col-6') isCol6: boolean;

    private isDroppable: boolean;

    @ViewChild("draggableContainer", {read: ViewContainerRef}) draggableContainer: ViewContainerRef;
    @ViewChild("dropzone") dropzoneElement: ElementRef;

    private id: string;
    private occupied: boolean = false;

    constructor() {
        this.isCol6 = true;
        this.setIsDroppable(false);
        this.id = uuid();


    }

    public ngOnInit() {
        switch (this.dropzoneModel.dropzoneType) {
            case DropzoneType.Toolbar:
                this.dropzoneElement.nativeElement.classList.add("dropzone-toolbar");
                break;
            case DropzoneType.Connector:
                this.dropzoneElement.nativeElement.classList.add("dropzone-connector");
                break;
            case DropzoneType.Workspace:
                this.dropzoneElement.nativeElement.classList.add("dropzone-workspace");
                break;
        }
    }

    public ngOnDestroy() {

    }

    computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone {
        if (!this.isDraggableInRange(mirror)) {
            return pivot;
        }
        if (pivot === null) {
            return this;
        }
        if (this.dropzoneModel.dropzoneType === DropzoneType.Workspace) {
            return pivot;
        }
        if (pivot.getDropzoneModel().dropzoneType === DropzoneType.Workspace) {
            return this;
        }

        const mirrorRectangle = mirror.getRectangle();
        const pivotRectangle = pivot.getRectangle();
        const rectangle = this.getRectangle();
        const pivotDistance = computeDistance(mirrorRectangle, pivotRectangle);
        const currentDistance = computeDistance(mirrorRectangle, rectangle);
        return currentDistance < pivotDistance ? this : pivot;

    }


    isDraggableInRange(draggable: Draggable): boolean {
        if (this.getOwner() === draggable ||this.isOccupied()) {
            return false;
        }
        if (this.dropzoneModel.dropzoneType === DropzoneType.Toolbar) {
            return false;
        }
        if (!this.dropzoneModel.acceptedDraggableTypes.includes(draggable.getDraggableModel().draggableType)) {
            return false;
        }
        if (
            draggable.getDraggableModel().initialDropzone.getDropzoneModel().dropzoneType !== DropzoneType.Workspace &&
            draggable.getDraggableModel().initialDropzone.getId() === this.id
        ) {
            return false;
        }
        const dropzoneNativeBoundingRect: ClientRect = this.dropzoneElement.nativeElement.getBoundingClientRect();

        const dropzoneBoundingBox: Rectangle = {
            left: dropzoneNativeBoundingRect.left - this.dropzoneModel.dropzoneRadius,
            right: dropzoneNativeBoundingRect.right + this.dropzoneModel.dropzoneRadius,
            top: dropzoneNativeBoundingRect.top - this.dropzoneModel.dropzoneRadius,
            bottom: dropzoneNativeBoundingRect.bottom + this.dropzoneModel.dropzoneRadius
        };

        const draggableBoundingBox: Rectangle = {
            left: draggable.getAbsoluteDraggablePosition().x,
            right: draggable.getAbsoluteDraggablePosition().x + draggable.getDraggableSize().width,
            top: draggable.getAbsoluteDraggablePosition().y,
            bottom: draggable.getAbsoluteDraggablePosition().y + draggable.getDraggableSize().height
        };

        return intersects(dropzoneBoundingBox, draggableBoundingBox);

    }


    getOwner(): any {
        return this.owner;
    }


    getIsDroppable(): boolean {
        return this.isDroppable;
    }

    setIsDroppable(isDroppable: boolean): void {
        this.isDroppable = isDroppable;
    }

    getDraggableContainer(): ViewContainerRef {
        return this.draggableContainer;
    }

    getId(): string {
        return this.id;
    }

    getDropzoneModel(): DropzoneModel {
        return this.dropzoneModel;
    }

    getAbsolutePosition(): { x: number, y: number } {
        const dropRect = this.dropzoneElement.nativeElement.getBoundingClientRect();
        return {x: dropRect.left, y: dropRect.top};
    }

    getSize(): { width: number, height: number } {
        return {
            width: this.dropzoneElement.nativeElement.offsetWidth,
            height: this.dropzoneElement.nativeElement.offsetHeight
        };
    }


    getRectangle(): Rectangle {
        const position = this.getAbsolutePosition();
        const size = this.getSize();
        return {
            top: position.y,
            left: position.x,
            right: position.x + size.width,
            bottom: position.y + size.height
        };
    }

    isOccupied(): boolean {
        return this.occupied;
    }

    occupyDropzone() {
        this.occupied = true;
    }

    clearDropzone() {
        this.occupied = false;
    }


}
