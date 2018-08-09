import {v4 as uuid} from "uuid";


export class Draggable {
    public id: string;
    public position: { x: number, y: number };
    public size: { width: number, height: number };
    public dropzoneType:string;

    constructor() {
        this.id = uuid();
    }


}