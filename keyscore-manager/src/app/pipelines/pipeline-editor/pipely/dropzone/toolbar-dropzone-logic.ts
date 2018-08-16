import {DropzoneLogic} from "./dropzone-logic";
import {Draggable, Dropzone} from "../models/contract";
import {Rectangle} from "../models/rectangle";
import {DropzoneComponent} from "../dropzone.component";

export class ToolbarDropzoneLogic extends DropzoneLogic {

    constructor() {
        super(null);
    }

    computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone {
        return pivot;
    }

    drop(mirror: Draggable = null, currentDragged: Draggable = null): void {
        return;
    }
}