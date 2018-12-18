import {ElementRef, ViewContainerRef} from "@angular/core";
import {DraggableModel} from "./draggable.model";
import {Observable} from "rxjs/index";
import {DropzoneModel} from "./dropzone.model";
import {Rectangle} from "./rectangle";
import {DropzoneSubcomponent} from "../dropzone/dropzone-subcomponent";

export interface Workspace {

    addDropzone(dropzone: Dropzone): void;

    removeAllDropzones(predicate: (dropzone: Dropzone) => boolean): void;

    removeDraggables(predicate: (draggable: Draggable) => boolean): void;

    registerDraggable(draggable: Draggable);

    getWorkspaceDropzone(): Dropzone;

    registerDraggable(draggable: Draggable);


}

export interface Dropzone {
    isDroppable$: Observable<boolean>;

    getId(): string;

    computeBestDropzone(mirror: Draggable, pivot: Dropzone): Dropzone;

    getDraggableContainer(): ViewContainerRef;

    getDropzoneElement(): ElementRef;

    getSubComponent(): DropzoneSubcomponent;

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

    detachNext();

    drop(mirrors: Draggable, currentDragged: Draggable): void;

}

export interface Draggable {
    dragStart$: Observable<MouseEvent>;

    getId(): string;

    getDraggablePosition(): { x: number, y: number };

    getAbsoluteDraggablePosition(): { x: number, y: number };

    getDraggableSize(): { width: number, height: number };

    getRectangle(): Rectangle;

    getDraggableModel(): DraggableModel;

    getNextConnection(): Dropzone;

    getNext(): Draggable;

    getPrevious(): Draggable;

    removeNextFromModel(): void;

    setNextModel(next: DraggableModel): void;

    getPreviousConnection(): Dropzone;

    destroy(): void;

    hide(): void;

    show(): void;

    isVisible(): boolean;

    getTail(): Draggable;

    getHead(): Draggable;

    getTotalWidth(): number;

    createNext();

    moveXAxis(deltaX: number): void;

    moveYAxis(deltaY: number): void;

    moveMirror(deltaX: number, deltaY: number): void;

    setLastDrag(x: number, y: number): void;

    getLastDrag(): { x: number, y: number };

    select(select: boolean): void;

    isSelected(): boolean;


}