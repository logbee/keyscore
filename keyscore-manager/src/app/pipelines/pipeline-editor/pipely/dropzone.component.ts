import {Component, ElementRef, HostBinding, Input, OnDestroy, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DropzoneModel} from "./models/dropzone.model";
import {Draggable, Dropzone, Workspace} from "./models/contract";
import {Rectangle} from "./models/rectangle";
import {DropzoneLogic} from "./dropzone/dropzone-logic";
import {DropzoneSubcomponent} from "./dropzone/dropzone-subcomponent";
import {DraggableFactory} from "./draggable/draggable-factory";

@Component({
    selector: "dropzone",
    template: `
        <div >
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
    @HostBinding('class.p-0') isP0: boolean;


    @ViewChild("dropzoneContainer", {read: ViewContainerRef}) dropzoneContainer: ViewContainerRef;


    private id: string;
    private occupied: boolean = false;

    constructor(public draggableFactory:DraggableFactory) {
        this.isCol12 = true;
        this.isP0 = true;
        this.id = uuid();
    }

    public ngOnInit() {
        this.setIsDroppable(false);

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
        return this.subComponent.isDroppable;
    }

    setIsDroppable(isDroppable: boolean): void {
        this.subComponent.isDroppable = isDroppable;
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
        const clientRect:ClientRect = this.getDropzoneElement().nativeElement.getBoundingClientRect();
        return {
            top: clientRect.top,
            left: clientRect.left,
            right: clientRect.right,
            bottom: clientRect.bottom,
            width: clientRect.width
        };
    }

    getRectangleWithRadius(): Rectangle {
        const rect = this.getRectangle();
        return {
            top: rect.top - this.dropzoneModel.dropzoneRadius,
            left: rect.left - this.dropzoneModel.dropzoneRadius,
            right: rect.right + this.dropzoneModel.dropzoneRadius,
            bottom: rect.bottom + this.dropzoneModel.dropzoneRadius,
            width: rect.right + 2*this.dropzoneModel.dropzoneRadius - rect.left
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

    getSubComponent():DropzoneSubcomponent{
        return this.subComponent;
    }

}
