import {DraggableModel} from "../models/draggable.model";

export class DragStartEvent {
    draggableModel: DraggableModel;
    mouseDownEvent: MouseEvent;

    constructor(draggableModel: DraggableModel, mouseEvent: MouseEvent) {
        this.draggableModel = draggableModel;
        this.mouseDownEvent = mouseEvent;
    }

}