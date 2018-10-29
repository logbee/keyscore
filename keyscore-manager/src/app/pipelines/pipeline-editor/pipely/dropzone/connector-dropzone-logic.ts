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

    computeDraggableModel(mirror: Draggable, currentDragged: Draggable): DraggableModel {

        if (this.isPreviousConnection()) {
            return this.prependModel(mirror, currentDragged);
        }
        else {
            return deepcopy(currentDragged.getDraggableModel());
        }

    }

    insertNewDraggable(draggableModel: DraggableModel) {
        if (this.isPreviousConnection()) {
            this.prependNewDraggable(draggableModel);
        } else {
            this.appendNewDraggable(draggableModel);
        }
    }

    private prependNewDraggable(draggableModel: DraggableModel) {
        this.component.draggableFactory
            .createDraggable(this.component.workspace.getWorkspaceDropzone().getDraggableContainer(),
                draggableModel,
                this.component.workspace);

        this.component.getOwner().destroy();
    }

    private appendNewDraggable(draggableModel: DraggableModel) {

        this.component.getOwner().setNextModel(draggableModel);
        this.component.getOwner().createNext();

    }

    private prependModel(mirror: Draggable, currentDragged: Draggable): DraggableModel {
        const droppedPosition = computeRelativePositionToParent(mirror.getAbsoluteDraggablePosition(),
            this.component.workspace.getWorkspaceDropzone().getAbsolutePosition());

        const nextModelCopy = deepcopy(this.component.getOwner().getDraggableModel());
        const nexDraggableModel = {
            ...nextModelCopy/*this.component.getOwner().getDraggableModel()*/,
            position: this.computePrependPosition()
        };

        let draggedCopy: DraggableModel = deepcopy(currentDragged.getDraggableModel());
        let draggedTailModel: DraggableModel = draggedCopy;
        while (draggedTailModel.next) {
            draggedTailModel = draggedTailModel.next;
        }
        draggedTailModel.next = nexDraggableModel;

        return {
            ...draggedCopy,
            initialDropzone: this.component.workspace.getWorkspaceDropzone(),
            rootDropzone: DropzoneType.Workspace,
            position: droppedPosition
        }

    }

    private computePrependPosition(): { x: number, y: number } {
        const componentRectangle = this.component.getRectangle();
        const ownerRectangle = this.component.getOwner().getRectangle();
        return {
            x: ownerRectangle.left - 20,
            y: componentRectangle.top
        };
    }

    isPreviousConnection(): boolean {
        return this.component.getOwner().getPreviousConnection().getId() === this.component.getId();
    }

    isNextConnection(): boolean{
        return this.component.getOwner().getNextConnection().getId() === this.component.getId();
    }
}