import {Draggable, Dropzone} from "./contract";
import {DropzoneType} from "./dropzone-type";
import {BlockDescriptor} from "./block-descriptor.model";
import {Ref,Configuration} from "@keyscore-manager-models";

export interface DraggableModel {
    blockDescriptor: BlockDescriptor;
    configuration: Configuration;
    blueprintRef: Ref;
    isMirror: boolean;
    isSelected: boolean;
    next: DraggableModel;
    previous: Draggable;
    initialDropzone: Dropzone;
    rootDropzone: DropzoneType;
    position?: { x: number, y: number };
    color?: string;

}

export const DRAGGABLE_WIDTH = 150;
export const DRAGGABLE_HEIGHT = 80;