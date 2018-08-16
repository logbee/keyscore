import {Draggable, Dropzone} from "../models/contract";
import {DropzoneType} from "../models/dropzone-type";
import {DropzoneComponent} from "../dropzone.component";
import {DraggableModel} from "../models/draggable.model";

export abstract class DropzoneLogic {

    constructor(protected component: DropzoneComponent){

    }

    abstract computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone;

    abstract drop(mirror: Draggable, currentDragged: Draggable): void;

    commonDrop(currentDragged: Draggable, draggableModel: DraggableModel) {
        const initialDropzone = currentDragged.getDraggableModel().initialDropzone;
        if (initialDropzone.getDropzoneModel().dropzoneType === DropzoneType.Connector) {
            initialDropzone.clearDropzone();
            initialDropzone.getOwner().removeNextFromModel();
            console.log("owner after remove next: ",initialDropzone.getOwner().getDraggableModel());

        }
        if (currentDragged.getDraggableModel().rootDropzone === DropzoneType.Workspace) {
            currentDragged.destroy();
        }

        const droppedDraggable = this.component.draggableFactory
            .createDraggable(this.component.getDraggableContainer(),
            draggableModel,
            this.component.workspace);

        this.component.workspace.registerDraggable(droppedDraggable);
    }
}