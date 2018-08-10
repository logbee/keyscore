import {Dropzone} from "./contract";

export interface DraggableModel {
    name: string;
    hasAbsolutePosition: boolean;
    draggableType: string;
    isMirror: boolean;
    initialDropzone:Dropzone;
    position?: { x: number, y: number };
}