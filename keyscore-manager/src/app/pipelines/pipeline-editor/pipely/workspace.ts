import {Dropzone} from "./dropzone";
import {v4 as uuid} from "uuid";
import {Draggable} from "./draggable";


export class Workspace {

    public id: string;
    public dropzones: Dropzone[];
    public currentMirror: Draggable;

    constructor(dropzones: Dropzone[] = []) {
        this.id = uuid();
        this.dropzones = dropzones;
    }

    public addDropzone(dropzone: Dropzone) {
        this.dropzones.push(dropzone);
    }

    public computeClosestDropzone(draggable: Draggable) {
        let closestDropzone: Dropzone = undefined;
        for (let dropzone of this.dropzones) {
            if (dropzone.acceptDraggable(draggable)) {
                closestDropzone = dropzone;
                break;
            }
        }
        return closestDropzone;
    }

}