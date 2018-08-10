import {Component, ElementRef, HostBinding, Input, OnDestroy, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DropzoneModel} from "./models/dropzone.model";
import {Draggable, Dropzone, Workspace} from "./models/contract";

@Component({
    selector: "dropzone",
    template: `
        <div #dropzone [class]="isDroppable ? 'dropzone is-droppable' : 'dropzone'">
            <ng-template #draggableContainer></ng-template>
        </div>
    `
})


export class DropzoneComponent implements OnInit, OnDestroy, Dropzone {

    @Input() workspace: Workspace;
    @Input() dropzoneModel: DropzoneModel;

    @HostBinding('class.col-6') isCol6: boolean;

    private isDroppable: boolean;

    @ViewChild("draggableContainer", {read: ViewContainerRef}) draggableContainer: ViewContainerRef;
    @ViewChild("dropzone") dropzoneElement: ElementRef;

    public id: string;

    constructor() {
        this.isCol6 = true;
        this.setIsDroppable(false);
        this.id = uuid();


    }

    public ngOnInit() {

    }

    public ngOnDestroy() {

    }

    isDraggableInRange(draggable: Draggable): boolean {
        this.isDroppable = this.acceptDraggable(draggable);
        return this.isDroppable;
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

    private acceptDraggable(draggable: Draggable): boolean {

        if (!this.dropzoneModel.acceptedDraggableTypes.includes(draggable.getDraggableModel().draggableType)) {
            return false;
        }
        let dropzoneNativeBoundingRect: ClientRect = this.dropzoneElement.nativeElement.getBoundingClientRect();

        const dropzoneBoundingBox = {
            left: dropzoneNativeBoundingRect.left - this.dropzoneModel.dropzoneRadius,
            right: dropzoneNativeBoundingRect.right + this.dropzoneModel.dropzoneRadius,
            top: dropzoneNativeBoundingRect.top - this.dropzoneModel.dropzoneRadius,
            bottom: dropzoneNativeBoundingRect.bottom + this.dropzoneModel.dropzoneRadius
        };

        const draggableBoundingBox = {
            left: draggable.getAbsoluteDraggablePosition().x,
            right: draggable.getAbsoluteDraggablePosition().x + draggable.getDraggableSize().width,
            top: draggable.getAbsoluteDraggablePosition().y,
            bottom: draggable.getAbsoluteDraggablePosition().y + draggable.getDraggableSize().height
        };

        return !(draggableBoundingBox.left > dropzoneBoundingBox.right ||
            draggableBoundingBox.right < dropzoneBoundingBox.left ||
            draggableBoundingBox.bottom < dropzoneBoundingBox.top ||
            draggableBoundingBox.top > dropzoneBoundingBox.bottom)

    }

    getId(): string {
        return this.id;
    }

    getDropzoneModel(): DropzoneModel {
        return this.dropzoneModel;
    }


}
