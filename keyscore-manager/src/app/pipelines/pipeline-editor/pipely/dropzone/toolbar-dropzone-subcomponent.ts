import {Component, ElementRef, ViewChild, ViewContainerRef} from "@angular/core";
import {DropzoneSubcomponent} from "./dropzone-subcomponent";

@Component({
    selector:"toolbar-dropzone",
    template:`
        <div #dropzone fxLayoutGap="15px" fxLayout="row" fxLayoutAlign="start start" [class.is-droppable]="isDroppable">
            <ng-template #draggableContainer></ng-template>
        </div>
    `
})

export class ToolbarDropzoneSubcomponent implements DropzoneSubcomponent{
    @ViewChild("draggableContainer", { read: ViewContainerRef, static: true }) draggableContainer: ViewContainerRef;
    @ViewChild("dropzone", { static: true }) dropzoneElement: ElementRef;

    isDroppable:boolean;
}