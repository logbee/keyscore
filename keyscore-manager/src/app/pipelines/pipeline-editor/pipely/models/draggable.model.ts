import {Draggable, Dropzone} from "./contract";
import {DropzoneType} from "./dropzone-type";
import {Connection} from "./connection.model";

export interface DraggableModel {
    name: string;
    hasAbsolutePosition: boolean;
    draggableType: string;
    previousConnection: Connection;
    parent:Draggable;
    nextConnection: Connection;
    isMirror: boolean;
    initialDropzone: Dropzone;
    rootDropzone: DropzoneType;
    position?: { x: number, y: number };
}