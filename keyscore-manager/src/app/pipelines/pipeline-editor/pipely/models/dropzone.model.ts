import {DropzoneType} from "./dropzone-type";
import {Draggable} from "./contract";

export interface DropzoneModel {
    dropzoneType: DropzoneType;
    acceptedDraggableTypes: string[];
    dropzoneRadius: number;
    owner:Draggable;
}