export interface DraggableModel {
    name: string;
    hasAbsolutePosition: boolean;
    dropzoneType: string;
    draggablePosition?: { x: number, y: number };
    draggableSize?: { width: number, height: number };
    isMirror?: boolean;
}