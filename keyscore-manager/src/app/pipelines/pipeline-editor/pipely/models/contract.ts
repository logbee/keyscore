import {ElementRef, ViewContainerRef} from "@angular/core";
import {DraggableModel} from "./draggable.model";
import {Observable} from "rxjs/index";
import {DropzoneModel} from "./dropzone.model";
import {Rectangle} from "./rectangle";

export interface Workspace {
    addDropzone(dropzone: Dropzone): void;

    removeAllDropzones(predicate: (dropzone: Dropzone) => boolean): void;

    registerDraggable(draggable: Draggable);

    registerMirror(mirror: Draggable);

    getWorkspaceDropzone():Dropzone;

}

export interface Dropzone {
    getId(): string;

    computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone;

    getDraggableContainer(): ViewContainerRef;

    getDropzoneElement(): ElementRef;

    getIsDroppable(): boolean;

    setIsDroppable(isDroppable: boolean): void;

    getDropzoneModel(): DropzoneModel;

    getAbsolutePosition(): { x: number, y: number };

    getSize(): { width: number, height: number };

    getRectangle(): Rectangle;

    getRectangleWithRadius(): Rectangle;

    getOwner(): Draggable;

    isOccupied(): boolean;

    occupyDropzone();

    clearDropzone();

    drop(mirros: Draggable, currentDragged: Draggable): void;

}

export interface Draggable {
    dragStart$: Observable<void>;
    dragMove$: Observable<void>;

    getId(): string;

    getDraggablePosition(): { x: number, y: number };

    getAbsoluteDraggablePosition(): { x: number, y: number };

    getDraggableSize(): { width: number, height: number };

    getRectangle(): Rectangle;

    getDraggableModel(): DraggableModel;

    getNextConnection(): Dropzone;

    getNext(): Draggable;

    removeNextFromModel():void;

    setNextModel(next:DraggableModel):void;

    getPreviousConnection(): Dropzone;

    destroy(): void;

    hide(): void;

    show(): void;

    isVisible(): boolean;


}