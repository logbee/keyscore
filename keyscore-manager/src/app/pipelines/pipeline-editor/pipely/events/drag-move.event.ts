export class DragMoveEvent {
    dropzoneType: string;
    position: { x: number, y: number };
    size: { width: number, height: number };

    constructor(position: { x: number, y: number },
                size: { width: number, height: number },
                dropzoneType: string) {
        this.position = position;
        this.size = size;
        this.dropzoneType = dropzoneType;
    }
}