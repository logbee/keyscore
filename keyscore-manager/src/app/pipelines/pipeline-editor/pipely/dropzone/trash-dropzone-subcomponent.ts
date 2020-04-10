import {Component, ElementRef, ViewChild, ViewContainerRef} from "@angular/core";
import {DropzoneSubcomponent} from "./dropzone-subcomponent";

@Component({
    selector:"connector-dropzone",
    template:`
        <div #dropzone class="dropzone-trash" [class.is-droppable]="isDroppable">
            <span></span>
            <i></i>
            <ng-template #draggableContainer></ng-template>
        </div>
    `
})

export class TrashDropzoneSubcomponent implements DropzoneSubcomponent{
    @ViewChild("draggableContainer", { read: ViewContainerRef, static: true }) draggableContainer: ViewContainerRef;
    @ViewChild("dropzone", { static: true }) dropzoneElement: ElementRef;

    isDroppable:boolean;

}