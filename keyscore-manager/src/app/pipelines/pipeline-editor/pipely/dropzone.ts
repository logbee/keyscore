import {v4 as uuid} from "uuid";
import {Draggable} from "./draggable";

export class Dropzone {
    public id: string;
    public draggables: Draggable[];
    public dropzoneRadius: number;
    public position: { x: number, y: number };
    public size: { width: number, height: number };
    public dropzoneType: string;

    constructor() {
        this.id = uuid();
    }

    public insertDraggable(draggable: Draggable) {
        this.draggables.push(draggable);
    }

    public removeDraggable(draggableID: string) {
        const indexToRemove = this.draggables.findIndex((draggable) => draggableID === draggable.id);
        this.draggables.splice(indexToRemove, 1);
    }

    public acceptDraggable(draggable: Draggable): boolean {
        if(draggable.dropzoneType != this.dropzoneType){
            return false;
        }
        const dropzoneBoundingBox = {
            left: this.position.x - this.dropzoneRadius,
            right: this.position.x + this.size.width + this.dropzoneRadius,
            top: this.position.y - this.dropzoneRadius,
            bottom: this.position.y + this.size.height + this.dropzoneRadius
        };

        const draggableBoundingBox = {
            left: draggable.position.x,
            right: draggable.position.x + draggable.size.width,
            top: draggable.position.y,
            bottom: draggable.position.y + draggable.size.height
        };

        return !(draggableBoundingBox.left > dropzoneBoundingBox.right ||
            draggableBoundingBox.right < dropzoneBoundingBox.left ||
            draggableBoundingBox.bottom < dropzoneBoundingBox.top ||
            draggableBoundingBox.top > dropzoneBoundingBox.bottom)

    }
}