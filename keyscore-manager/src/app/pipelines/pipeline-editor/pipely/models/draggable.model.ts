import {Draggable, Dropzone} from "./contract";
import {DropzoneType} from "./dropzone-type";
import {BlockDescriptor} from "./block-descriptor.model";
import {BlockConfiguration} from "./block-configuration.model";

export interface DraggableModel {
    blockDescriptor:BlockDescriptor;
    blockConfiguration:BlockConfiguration;
    isMirror: boolean;
    next:DraggableModel;
    previous:Draggable;
    initialDropzone: Dropzone;
    rootDropzone: DropzoneType;
    position?: { x: number, y: number };
}