import {Component, ElementRef, HostBinding, Input, OnDestroy, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DropzoneModel} from "./models/dropzone.model";
import {Draggable, Dropzone, Workspace} from "./models/contract";
import {Rectangle} from "./models/rectangle";
import {DropzoneLogic} from "./dropzone/dropzone-logic";
import {DropzoneSubcomponent} from "./dropzone/dropzone-subcomponent";
import {DropzoneFactory} from "./dropzone/dropzone-factory";
import {DraggableFactory} from "./draggable/draggable-factory";

@Component({
    selector: "dropzone",
    template: `
        <div [class.is-droppable]="isDroppable">
            <ng-template #dropzoneContainer></ng-template>
        </div>

    `
})

export class DropzoneComponent implements OnInit, OnDestroy, Dropzone {

    @Input() dropzoneModel: DropzoneModel;
    @Input() logic: DropzoneLogic;
    @Input() subComponent: DropzoneSubcomponent;
    @Input() workspace:Workspace;

    @HostBinding('class.col-12') isCol12: boolean;

    private isDroppable: boolean;

    @ViewChild("dropzoneContainer", {read: ViewContainerRef}) dropzoneContainer: ViewContainerRef;


    private id: string;
    private occupied: boolean = false;

    constructor(public draggableFactory:DraggableFactory) {
        this.isCol12 = true;
        this.setIsDroppable(false);
        this.id = uuid();
    }

    public ngOnInit() {

    }

    public ngOnDestroy() {

    }

    computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone {
        return this.logic.computeBestDropzone(mirror, pivot);

    }

    getOwner(): any {
        return this.dropzoneModel.owner;
    }

    drop(mirror: Draggable, currentDragged: Draggable) {
        this.logic.drop(mirror, currentDragged);
    }

    getIsDroppable(): boolean {
        return this.isDroppable;
    }

    setIsDroppable(isDroppable: boolean): void {
        this.isDroppable = isDroppable;
    }

    getId(): string {
        return this.id;
    }

    getDropzoneModel(): DropzoneModel {
        return this.dropzoneModel;
    }

    getAbsolutePosition(): { x: number, y: number } {
        const dropRect = this.getDropzoneElement().nativeElement.getBoundingClientRect();
        return {x: dropRect.left, y: dropRect.top};
    }

    getSize(): { width: number, height: number } {
        return {
            width: this.getDropzoneElement().nativeElement.offsetWidth,
            height: this.getDropzoneElement().nativeElement.offsetHeight
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

    getRectangleWithRadius(): Rectangle {
        const position = this.getAbsolutePosition();
        const size = this.getSize();
        return {
            top: position.y - this.dropzoneModel.dropzoneRadius,
            left: position.x - this.dropzoneModel.dropzoneRadius,
            right: position.x + size.width + this.dropzoneModel.dropzoneRadius,
            bottom: position.y + size.height + this.dropzoneModel.dropzoneRadius
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

    getDraggableContainer(): ViewContainerRef {
        return this.subComponent.draggableContainer;
    }

    getDropzoneElement(): ElementRef {
        return this.subComponent.dropzoneElement;
    }

}
