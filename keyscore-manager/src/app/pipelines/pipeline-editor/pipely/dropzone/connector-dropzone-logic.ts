import {DropzoneLogic} from "./dropzone-logic";
import {Draggable, Dropzone} from "../models/contract";
import {DropzoneComponent} from "../dropzone.component";
import {Rectangle} from "../models/rectangle";
import {computeDistance, computeRelativePositionToParent, intersects} from "../util/util";
import {DropzoneType} from "../models/dropzone-type";
import {DraggableModel} from "../models/draggable.model";
import {deepcopy} from "../../../../util";

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

        if (this.isPreviousConnection()) {
            return this.prependModel(mirror, currentDragged);
        }
        else {
            return this.appendModel(mirror, currentDragged);

        }

    }

    insertNewDraggable(draggableModel: DraggableModel) {
        if (this.isPreviousConnection()) {
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

        let draggedCopy:DraggableModel = deepcopy(currentDragged.getDraggableModel());
        let draggedTailModel: DraggableModel = draggedCopy;
        while (draggedTailModel.next) {
            draggedTailModel = draggedTailModel.next;
        }
        draggedTailModel.next = nexDraggableModel;

        return{
            ...draggedCopy,
            initialDropzone: this.component.workspace.getWorkspaceDropzone(),
            rootDropzone: DropzoneType.Workspace,
            position: droppedPosition
        }

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

    private isPreviousConnection(): boolean {
        return this.component.getOwner().getPreviousConnection().getId() === this.component.getId();
    }
}