import {DropzoneLogic} from "./dropzone-logic";
import {Draggable, Dropzone} from "../models/contract";
import {DraggableModel} from "../models/draggable.model";

export class ToolbarDropzoneLogic extends DropzoneLogic {

    constructor() {
        super(null);
    }

    computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone {
        return pivot;
    }

    computeDraggableModel(mirror: Draggable = null, currentDragged: Draggable = null): DraggableModel {
        return null;
    }

    drop(mirror: Draggable = null, currentDragged: Draggable = null): void {
        return;
    }

}