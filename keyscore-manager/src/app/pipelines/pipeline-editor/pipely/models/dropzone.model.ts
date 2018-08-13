import {DropzoneType} from "./dropzone-type";

export interface DropzoneModel {
    dropzoneType: DropzoneType;
    acceptedDraggableTypes: string[];
    dropzoneRadius: number;
}