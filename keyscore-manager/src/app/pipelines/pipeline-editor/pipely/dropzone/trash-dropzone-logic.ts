import {DropzoneLogic} from "./dropzone-logic";
import {Draggable} from "../models/contract";
import {DropzoneComponent} from "../dropzone.component";
import {DraggableModel} from "../models/draggable.model";
import {DropzoneType} from "../models/dropzone-type";

export class TrashDropzoneLogic extends DropzoneLogic {

    constructor(component: DropzoneComponent) {
        super(component);
    }

    computeDraggableModel(mirror: Draggable = null, currentDragged: Draggable = null): DraggableModel {
        return null;
    }

    drop(mirror: Draggable = null, currentDragged: Draggable = null): void {
        this.component.setIsDroppable(false);
        const initialDropzone = currentDragged.getDraggableModel().initialDropzone;
        if (initialDropzone.getDropzoneModel().dropzoneType === DropzoneType.Connector) {
            initialDropzone.detachNext();
        }
        if (currentDragged.getDraggableModel().initialDropzone.getDropzoneModel().dropzoneType !== DropzoneType.Toolbar) {
            currentDragged.destroy();
        }

        /*const deletModelPosition = computeRelativePositionToParent(mirror.getAbsoluteDraggablePosition(),
            this.component.workspace.getWorkspaceDropzone().getAbsolutePosition())
        const deleteModel = {...mirror.getDraggableModel(),
            position: deletModelPosition,
            isMirror: false,
            draggableType:"delete",
        initialDropzone:this.component.workspace.getWorkspaceDropzone()
        };
        const deleteDraggable = this.component.draggableFactory.createDraggable(
            this.component.workspace.getWorkspaceDropzone().getDraggableContainer(),
            deleteModel, this.component.workspace);*/
    }

}