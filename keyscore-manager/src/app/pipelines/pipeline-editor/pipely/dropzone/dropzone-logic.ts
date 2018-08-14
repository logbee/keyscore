import {Draggable, Dropzone} from "../models/contract";
import {Rectangle} from "../models/rectangle";

export interface DropzoneLogic {
    computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone;

    // drop(mirror:Draggable):void;
}