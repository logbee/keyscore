import {Draggable, Dropzone} from "./contract";
import {DropzoneType} from "./dropzone-type";
import {BlockDescriptor} from "./block-descriptor.model";
import {BlockConfiguration} from "./block-configuration.model";
import {Ref} from "../../../../models/common/Ref";

export interface DraggableModel {
    blockDescriptor: BlockDescriptor;
    blockConfiguration: BlockConfiguration;
    blueprintRef: Ref;
    isMirror: boolean;
    next: DraggableModel;
    previous: Draggable;
    initialDropzone: Dropzone;
    rootDropzone: DropzoneType;
    position?: { x: number, y: number };
    color?: string;

}

export const DRAGGABLE_WIDTH = 150;
export const DRAGGABLE_HEIGHT = 80;