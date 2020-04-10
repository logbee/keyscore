import {DropzoneLogic} from "./dropzone-logic";
import {Draggable, Dropzone} from "../models/contract";
import {computeRelativePositionToParent, intersects} from "../util/util";
import {Rectangle} from "../models/rectangle";
import {DropzoneComponent} from "../dropzone.component";
import {DropzoneType} from "../models/dropzone-type";
import {cloneDeep} from 'lodash-es';

export class WorkspaceDropzoneLogic extends DropzoneLogic {

    constructor(component: DropzoneComponent) {
        super(component);
    }

    computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone {
        if (!this.isMirrorInRange(mirror)) {
            return pivot;
        }
        return pivot === null ? this.component : pivot;
    }

    computeDraggableModel(mirror: Draggable, currentDragged: Draggable) {
        let draggableModel = cloneDeep(currentDragged.getDraggableModel());
        draggableModel.initialDropzone = this.component;
        draggableModel.rootDropzone = DropzoneType.Workspace;
        draggableModel.position = computeRelativePositionToParent(mirror.getAbsoluteDraggablePosition(),
            this.component.getAbsolutePosition());

        return draggableModel;
    }


    isMirrorInRange(mirror: Draggable): boolean {

        const dropzoneBoundingBox: Rectangle = this.component.getRectangleWithRadius();

        const draggableBoundingBox: Rectangle = mirror.getRectangle();

        return intersects(dropzoneBoundingBox, draggableBoundingBox);
    }
}
