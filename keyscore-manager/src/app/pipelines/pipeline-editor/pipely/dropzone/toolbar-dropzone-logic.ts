import {DropzoneLogic} from "./dropzone-logic";
import {Draggable, Dropzone} from "../models/contract";
import {Rectangle} from "../models/rectangle";

export class ToolbarDropzoneLogic implements DropzoneLogic {

    computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone {
        return pivot;
    }
}