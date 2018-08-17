import {Draggable, Dropzone} from "../models/contract";
import {DropzoneType} from "../models/dropzone-type";
import {DropzoneComponent} from "../dropzone.component";
import {DraggableModel} from "../models/draggable.model";

export abstract class DropzoneLogic {

    constructor(protected component: DropzoneComponent) {

    }

    abstract computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone;

    abstract computeDraggableModel(mirror: Draggable, currentDragged: Draggable): DraggableModel;

    insertNewDraggable(draggableModel: DraggableModel) {
        return;
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

        const droppedDraggable = this.component.draggableFactory
            .createDraggable(this.component.getDraggableContainer(),
                draggableModel,
                this.component.workspace);

        this.component.workspace.registerDraggable(droppedDraggable);
    }
}