import {Draggable, Dropzone} from "../models/contract";
import {DropzoneType} from "../models/dropzone-type";
import {DropzoneComponent} from "../dropzone.component";
import {DraggableModel} from "../models/draggable.model";
import {computeDistance, intersects} from "../util/util";
import {Rectangle} from "../models/rectangle";
import {generateRef} from "../../../../models/common/Ref";

export abstract class DropzoneLogic {

    constructor(protected component: DropzoneComponent) {

    }

    abstract computeDraggableModel(mirror: Draggable, currentDragged: Draggable): DraggableModel;

    isPreviousConnection(): boolean {
        return false;
    }

    isNextConnection(): boolean {
        return false;
    }

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
        let nextDraggable = mirror;
        let mirrorTail = null;
        do {
            if (nextDraggable.getNextConnection() &&
                nextDraggable.getNextConnection().getId() === this.component.getId()) {
                return false;
            }
            mirrorTail = nextDraggable;
        } while (nextDraggable = nextDraggable.getNext());

        if (this.isNextConnection() &&
            !this.component.dropzoneModel.acceptedDraggableTypes.includes("all") &&
            !this.component.dropzoneModel.acceptedDraggableTypes.includes(mirror.getDraggableModel().blockDescriptor.previousConnection.connectionType)) {
            return false;
        }
        if (this.isPreviousConnection() &&
            !this.component.dropzoneModel.acceptedDraggableTypes.includes("all") &&
            !this.component.dropzoneModel.acceptedDraggableTypes.includes(mirrorTail.getDraggableModel().blockDescriptor.nextConnection.connectionType)) {
            return false;
        }

        if (this.component.getOwner() === mirror) {
            return false;
        }

        if (this.component.isOccupied() && mirror.getDraggableModel().initialDropzone.getId() !== this.component.getId()) {
            return false;
        }

        const dropzoneBoundingBox: Rectangle = this.component.getRectangleWithRadius();
        let draggableBoundingBox: Rectangle = this.isPreviousConnection() ? mirrorTail.getRectangle(): mirror.getRectangle();

        return intersects(dropzoneBoundingBox, draggableBoundingBox);
    }


    drop(mirror: Draggable, currentDragged: Draggable) {

        this.component.setIsDroppable(false);

        const initialDropzone = currentDragged.getDraggableModel().initialDropzone;
        if (initialDropzone.getDropzoneModel().dropzoneType === DropzoneType.Connector) {
            console.log("DETACH");
            initialDropzone.detachNext();
        }
        if (currentDragged.getDraggableModel().rootDropzone === DropzoneType.Workspace) {
            currentDragged.destroy();
        }

        const draggableModel = this.computeDraggableModel(mirror, currentDragged);
        if(currentDragged.getDraggableModel().initialDropzone.getDropzoneModel().dropzoneType === DropzoneType.Toolbar){
            draggableModel.configuration.ref = generateRef();
            console.log("DROP REF:",draggableModel.configuration.ref);
        }
        this.insertNewDraggable(draggableModel);

    }

    insertNewDraggable(draggableModel: DraggableModel) {
        this.component.draggableFactory
            .createDraggable(this.component.getDraggableContainer(),
                draggableModel,
                this.component.workspace);

    }
}