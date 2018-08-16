import {DropzoneLogic} from "./dropzone-logic";
import {Draggable, Dropzone} from "../models/contract";
import {computeRelativePositionToParent, intersects} from "../util/util";
import {Rectangle} from "../models/rectangle";
import {DropzoneComponent} from "../dropzone.component";
import {DropzoneType} from "../models/dropzone-type";

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

    drop(mirror: Draggable, currentDragged: Draggable): void {
        this.component.setIsDroppable(false);
        const draggableModel = {
            ...currentDragged.getDraggableModel(),
            initialDropzone: this.component,
            rootDropzone: DropzoneType.Workspace,
            position: computeRelativePositionToParent(mirror.getAbsoluteDraggablePosition(),
                this.component.getAbsolutePosition())
        };
        console.log("dropped DraggableModel:",draggableModel);
        this.commonDrop(currentDragged, draggableModel);

    }

    private isMirrorInRange(mirror: Draggable): boolean {

        const dropzoneBoundingBox: Rectangle = this.component.getRectangleWithRadius();

        const draggableBoundingBox: Rectangle = mirror.getRectangle();

        return intersects(dropzoneBoundingBox, draggableBoundingBox);
    }
}