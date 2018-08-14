import {Component, ElementRef, ViewChild, ViewContainerRef} from "@angular/core";
import {DropzoneSubcomponent} from "./dropzone-subcomponent";

@Component({
    selector:"connector-dropzone",
    template:`
        <div #dropzone class="dropzone-connector" [class.is-droppable]="isDroppable">
            <ng-template #draggableContainer></ng-template>
        </div>
    `
})

export class ConnectorDropzoneSubcomponent implements DropzoneSubcomponent{
    @ViewChild("draggableContainer", {read: ViewContainerRef}) draggableContainer: ViewContainerRef;
    @ViewChild("dropzone") dropzoneElement: ElementRef;

}