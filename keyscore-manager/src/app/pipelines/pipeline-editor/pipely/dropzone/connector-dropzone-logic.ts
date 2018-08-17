import {DropzoneLogic} from "./dropzone-logic";
import {Draggable, Dropzone} from "../models/contract";
import {DropzoneComponent} from "../dropzone.component";
import {Rectangle} from "../models/rectangle";
import {computeDistance, computeRelativePositionToParent, intersects} from "../util/util";
import {DropzoneType} from "../models/dropzone-type";
import {DraggableModel} from "../models/draggable.model";

export class ConnectorDropzoneLogic extends DropzoneLogic {

    constructor(component: DropzoneComponent) {
        super(component);

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

    computeDraggableModel(mirror: Draggable, currentDragged: Draggable): DraggableModel {

        if (this.component.getOwner().getPreviousConnection().getId() === this.component.getId()) {
            return this.prependModel(mirror, currentDragged);
        }
        else {
            return this.appendModel(mirror, currentDragged);

        }

    }

    insertNewDraggable(draggableModel: DraggableModel) {
        if (this.component.getOwner().getPreviousConnection().getId() === this.component.getId()) {
            this.prependNewDraggable(draggableModel);
        } else {
            this.appendNewDraggable(draggableModel);
        }
    }

    private prependModel(mirror: Draggable, currentDragged: Draggable): DraggableModel {
        const droppedPosition = computeRelativePositionToParent(mirror.getAbsoluteDraggablePosition(),
            this.component.workspace.getWorkspaceDropzone().getAbsolutePosition());

        const nexDraggableModel = {
            ...this.component.getOwner().getDraggableModel(),
            position: this.computePrependPosition(droppedPosition, currentDragged.getDraggableSize().width)
        };

        return {
            ...currentDragged.getDraggableModel(),
            initialDropzone: this.component.workspace.getWorkspaceDropzone(),
            rootDropzone: DropzoneType.Workspace,
            next: nexDraggableModel,
            position: droppedPosition
        };
    }

    private appendModel(mirror: Draggable, currentDragged: Draggable): DraggableModel {
        return {
            ...currentDragged.getDraggableModel(),
            initialDropzone: this.component,
            rootDropzone: DropzoneType.Workspace,
            position: this.computeAppendPosition()
        };
    }

    private prependNewDraggable(draggableModel: DraggableModel) {
        const droppedDraggable = this.component.draggableFactory
            .createDraggable(this.component.workspace.getWorkspaceDropzone().getDraggableContainer(),
                draggableModel,
                this.component.workspace);

        this.component.workspace.registerDraggable(droppedDraggable);
        this.component.getOwner().destroy();
    }

    private appendNewDraggable(draggableModel: DraggableModel) {
        this.component.occupyDropzone();
        this.component.getOwner().setNextModel(draggableModel);

        const droppedDraggable = this.component.draggableFactory
            .createDraggable(this.component.getDraggableContainer(),
                draggableModel,
                this.component.workspace);

        this.component.workspace.registerDraggable(droppedDraggable);

    }

    private computeAppendPosition(): { x: number, y: number } {
        const componentRectangle = this.component.getRectangle();
        const ownerRectangle = this.component.getOwner().getRectangle();
        return {
            x: Math.abs(ownerRectangle.right - componentRectangle.left),
            y: -Math.abs(componentRectangle.top - ownerRectangle.top)
        };
    }

    private computePrependPosition(droppedPosition: { x: number, y: number }, draggedWidth: number): { x: number, y: number } {
        const componentRectangle = this.component.getRectangle();
        const ownerRectangle = this.component.getOwner().getRectangle();
        return {
            x: Math.abs(ownerRectangle.left - componentRectangle.right),
            y: -Math.abs(componentRectangle.top - ownerRectangle.top)
        };
    }


    private isMirrorInRange(mirror: Draggable): boolean {
        if (!this.component.dropzoneModel.acceptedDraggableTypes.includes(mirror.getDraggableModel().draggableType)) {
            return false;
        }

        if (this.component.getOwner() === mirror) {
            return false;
        }

        if(this.component.isOccupied() && mirror.getDraggableModel().initialDropzone.getId() !== this.component.getId()){
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