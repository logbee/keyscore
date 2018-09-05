import {Draggable, Dropzone} from "../models/contract";
import {DropzoneType} from "../models/dropzone-type";
import {DropzoneComponent} from "../dropzone.component";
import {DraggableModel} from "../models/draggable.model";
import {computeDistance, intersects} from "../util/util";
import {Rectangle} from "../models/rectangle";

export abstract class DropzoneLogic {

    constructor(protected component: DropzoneComponent) {

    }

    abstract computeDraggableModel(mirror: Draggable, currentDragged: Draggable): DraggableModel;

    abstract isPreviousConnection():boolean;

    computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone {
        if (!this.isMirrorInRange(mirror)) {
            return pivot;
        }
        if (pivot === null) {
            return this.component;
        }
        if (pivot.getDropzoneModel().dropzoneType === DropzoneType.Workspace) {
            return this.component;
        }

        const mirrorRectangle = mirror.getRectangle();
        const pivotRectangle = pivot.getRectangle();
        const rectangle = this.component.getRectangle();

        const pivotDistance = computeDistance(mirrorRectangle, pivotRectangle);
        const currentDistance = computeDistance(mirrorRectangle, rectangle);

        return currentDistance < pivotDistance ? this.component : pivot;
    }

    isMirrorInRange(mirror: Draggable): boolean {
        if (!this.component.dropzoneModel.acceptedDraggableTypes.includes("all") &&
            !this.component.dropzoneModel.acceptedDraggableTypes.includes(mirror.getDraggableModel().draggableType)) {
            return false;
        }

        if (this.component.getOwner() === mirror) {
            return false;
        }

        if (this.component.isOccupied() && mirror.getDraggableModel().initialDropzone.getId() !== this.component.getId()) {
            return false;
        }

        let nextDraggable = mirror;
        let mirrorTail = null;
        do {
            if (nextDraggable.getNextConnection() &&
                nextDraggable.getNextConnection().getId() === this.component.getId()) {
                return false;
            }
            mirrorTail = nextDraggable;
        } while (nextDraggable = nextDraggable.getNext());

        const dropzoneBoundingBox: Rectangle = this.component.getRectangleWithRadius();
        let draggableBoundingBox: Rectangle = mirror.getRectangle();

        if (this.isPreviousConnection() && mirror.getDraggableModel().next) {
            draggableBoundingBox = mirrorTail.getRectangle();
        }

        return intersects(dropzoneBoundingBox, draggableBoundingBox);
    }



    drop(mirror: Draggable, currentDragged: Draggable) {

        this.component.setIsDroppable(false);

        const initialDropzone = currentDragged.getDraggableModel().initialDropzone;
        if (initialDropzone.getDropzoneModel().dropzoneType === DropzoneType.Connector) {
            initialDropzone.clearDropzone();
            initialDropzone.getOwner().removeNextFromModel();

        }
        if (currentDragged.getDraggableModel().rootDropzone === DropzoneType.Workspace) {
            currentDragged.destroy();
        }

        const draggableModel = this.computeDraggableModel(mirror, currentDragged);
        this.insertNewDraggable(draggableModel);

    }

    insertNewDraggable(draggableModel: DraggableModel) {
        const droppedDraggable = this.component.draggableFactory
            .createDraggable(this.component.getDraggableContainer(),
                draggableModel,
                this.component.workspace);

    }
}