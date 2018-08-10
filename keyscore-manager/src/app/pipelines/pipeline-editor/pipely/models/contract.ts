import {ViewContainerRef} from "@angular/core";
import {DraggableModel} from "./draggable.model";
import {Observable} from "rxjs/index";
import {DropzoneModel} from "./dropzone.model";

export interface Workspace {

}

export interface Dropzone {
    getId(): string;

    isDraggableInRange(draggable: Draggable): boolean;

    getDraggableContainer(): ViewContainerRef;

    getIsDroppable(): boolean;

    setIsDroppable(isDroppable: boolean): void;

    getDropzoneModel(): DropzoneModel;
}

export interface Draggable {
    dragStart$: Observable<void>;
    dragMove$: Observable<void>;

    getId(): string;

    getDraggablePosition(): { x: number, y: number };

    getAbsoluteDraggablePosition(): { x: number, y: number };

    getDraggableSize(): { width: number, height: number };

    getDraggableModel(): DraggableModel;


}