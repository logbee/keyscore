import {Component, ElementRef, ViewChild, ViewContainerRef} from "@angular/core";
import {DropzoneSubcomponent} from "./dropzone-subcomponent";

@Component({
    selector: "workspace-dropzone",
    template: `
        <div class="workspace-scroll-wrapper">
            <div #dropzone [class]="'dropzone-workspace'">
                <ng-template #draggableContainer></ng-template>
            </div>
        </div>
    `
})

export class WorkspaceDropzoneSubcomponent implements DropzoneSubcomponent {

    @ViewChild("draggableContainer", {read: ViewContainerRef}) draggableContainer: ViewContainerRef;
    @ViewChild("dropzone") dropzoneElement: ElementRef;

    isDroppable: boolean;
}