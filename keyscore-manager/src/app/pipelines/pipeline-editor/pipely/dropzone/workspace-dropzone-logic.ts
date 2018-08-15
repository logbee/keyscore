import {DropzoneLogic} from "./dropzone-logic";
import {Draggable, Dropzone} from "../models/contract";
import {computeRelativePositionToParent, intersects} from "../util/util";
import {Rectangle} from "../models/rectangle";
import {DropzoneComponent} from "../dropzone.component";
import {DropzoneType} from "../models/dropzone-type";

export class WorkspaceDropzoneLogic implements DropzoneLogic {

    constructor(private component: DropzoneComponent) {

    }

    computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone {
        if (!this.isMirrorInRange(mirror)) {
            return pivot;
        }
        return pivot === null ? this.component : pivot;
    }

    drop(mirror: Draggable, currentDragged: Draggable):void {
        this.component.setIsDroppable(false);
        const draggableModel = {
            ...currentDragged.getDraggableModel(),
            initialDropzone: this.component,
            rootDropzone: DropzoneType.Workspace,
            position: computeRelativePositionToParent(mirror.getAbsoluteDraggablePosition(),
                this.component.getAbsolutePosition())
        };
        const initialDropzone = currentDragged.getDraggableModel().initialDropzone;
        if (initialDropzone.getDropzoneModel().dropzoneType === DropzoneType.Connector) {
            initialDropzone.clearDropzone();
            console.log("model before remove:",initialDropzone.getOwner().getDraggableModel());
            initialDropzone.getOwner().removeNextFromModel();
            console.log("model after remove:",initialDropzone.getOwner().getDraggableModel());

        }
        if (currentDragged.getDraggableModel().rootDropzone === DropzoneType.Workspace) {
            currentDragged.destroy();
        }


        const droppedDraggable =  this.component.draggableFactory.createDraggable(this.component.getDraggableContainer(),
            draggableModel,
            this.component.workspace);
        this.component.workspace.registerDraggable(droppedDraggable);

    }

    private isMirrorInRange(mirror: Draggable): boolean {

        const dropzoneBoundingBox: Rectangle = this.component.getRectangleWithRadius();

        const draggableBoundingBox: Rectangle = mirror.getRectangle();

        return intersects(dropzoneBoundingBox, draggableBoundingBox);
    }
}