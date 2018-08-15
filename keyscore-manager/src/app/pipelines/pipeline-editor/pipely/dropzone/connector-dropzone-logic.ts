import {DropzoneLogic} from "./dropzone-logic";
import {Draggable, Dropzone} from "../models/contract";
import {DropzoneComponent} from "../dropzone.component";
import {Rectangle} from "../models/rectangle";
import {computeDistance, computeRelativePositionToParent, intersects} from "../util/util";
import {DropzoneType} from "../models/dropzone-type";

export class ConnectorDropzoneLogic implements DropzoneLogic {

    constructor(private component: DropzoneComponent) {

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

    drop(mirror: Draggable, currentDragged: Draggable): void {
        this.component.setIsDroppable(false);
        const draggableModel = {
            ...currentDragged.getDraggableModel(),
            initialDropzone: this.component,
            rootDropzone: DropzoneType.Workspace
        };

        this.component.occupyDropzone();
        this.component.getOwner().getDraggableModel().next = draggableModel;

        const initialDropzone = mirror.getDraggableModel().initialDropzone;
        if (initialDropzone.getDropzoneModel().dropzoneType === DropzoneType.Connector) {
            initialDropzone.clearDropzone();
        }
        if (currentDragged.getDraggableModel().rootDropzone === DropzoneType.Workspace) {
            currentDragged.destroy();
        }

        const droppedDraggable = this.component.draggableFactory.createDraggable(this.component.getDraggableContainer(),
            draggableModel,
            this.component.workspace);

        this.component.workspace.registerDraggable(droppedDraggable);


    }

    private isMirrorInRange(mirror: Draggable): boolean {
        if (!this.component.dropzoneModel.acceptedDraggableTypes.includes(mirror.getDraggableModel().draggableType)) {
            return false;
        }
        if (mirror.getDraggableModel().initialDropzone.getId() === this.component.getId()) {
            return false;
        }

        if (this.component.getOwner() === mirror || this.component.isOccupied()) {
            return false;
        }

        let nextDraggable = mirror;
        do {
            if (nextDraggable.getNextConnection() &&
                nextDraggable.getNextConnection().getId() === this.component.getId()) {
                return false;
            }
        } while (nextDraggable = nextDraggable.getNext());


        const dropzoneBoundingBox: Rectangle = this.component.getRectangleWithRadius();

        const draggableBoundingBox: Rectangle = mirror.getRectangle();

        return intersects(dropzoneBoundingBox, draggableBoundingBox);
    }
}