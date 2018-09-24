import {Draggable, Dropzone} from "./contract";
import {DropzoneType} from "./dropzone-type";
import {BlockDescriptor} from "./block-descriptor.model";
import {BlockConfiguration} from "./block-configuration.model";
import {Ref} from "../../../../models/common/Ref";

export interface DraggableModel {
    blockDescriptor:BlockDescriptor;
    blockConfiguration:BlockConfiguration;
    blueprintRef:Ref;
    color:string;
    isMirror: boolean;
    next:DraggableModel;
    previous:Draggable;
    initialDropzone: Dropzone;
    rootDropzone: DropzoneType;
    position?: { x: number, y: number };
}