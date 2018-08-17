import {ElementRef, ViewContainerRef} from "@angular/core";

export interface DropzoneSubcomponent{
    draggableContainer: ViewContainerRef;
    dropzoneElement: ElementRef;
    isDroppable: boolean;
}