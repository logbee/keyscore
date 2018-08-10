import {Component, ElementRef, HostBinding, Input, OnDestroy, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {WorkspaceComponent} from "./workspace.component";
import {v4 as uuid} from "uuid";
import {DragService} from "./services/drag.service";
import {DragMoveEvent} from "./events/drag-move.event";
import {DropzoneModel} from "./models/dropzone.model";

@Component({
    selector: "dropzone",
    template: `
        <div #dropzone [class]="isDroppable ? 'dropzone is-droppable' : 'dropzone'">
            <ng-template #draggableContainer></ng-template>
        </div>
    `
})

export class DropzoneComponent implements OnInit, OnDestroy {

    @Input() workspace: WorkspaceComponent;
    @Input() dropzoneModel: DropzoneModel;

    @HostBinding('class.col-6') isCol6: boolean;
    isDroppable: boolean;

    @ViewChild("draggableContainer", {read: ViewContainerRef}) draggableContainer: ViewContainerRef;
    @ViewChild("dropzone") dropzoneElement: ElementRef;

    public id: string;

    constructor(private dragService: DragService) {
        this.isCol6 = true;
        this.isDroppable = false;
        this.id = uuid();

        this.dragService.dragMove$.subscribe(moveEvent => {
            this.isDroppable = this.acceptDraggable(moveEvent);
        })

    }

    public ngOnInit() {

    }

    public ngOnDestroy() {

    }

    private acceptDraggable(moveEvent: DragMoveEvent): boolean {
        if (moveEvent.dropzoneType != this.dropzoneModel.dropzoneType) {
            return false;
        }
        let dropzoneNativeElement = this.dropzoneElement.nativeElement;
        const dropzoneBoundingBox = {
            left: dropzoneNativeElement.offsetLeft - this.dropzoneModel.dropzoneRadius,
            right: dropzoneNativeElement.offsetLeft + dropzoneNativeElement.offsetWidth + this.dropzoneModel.dropzoneRadius,
            top: dropzoneNativeElement.offsetTop - this.dropzoneModel.dropzoneRadius,
            bottom: dropzoneNativeElement.offsetTop + dropzoneNativeElement.offsetHeight + this.dropzoneModel.dropzoneRadius
        };

        const draggableBoundingBox = {
            left: moveEvent.position.x,
            right: moveEvent.position.x + moveEvent.size.width,
            top: moveEvent.position.y,
            bottom: moveEvent.position.y + moveEvent.size.height
        };

        console.log("Dropzone: ", dropzoneBoundingBox);
        console.log("Draggable: ", draggableBoundingBox);

        return !(draggableBoundingBox.left > dropzoneBoundingBox.right ||
            draggableBoundingBox.right < dropzoneBoundingBox.left ||
            draggableBoundingBox.bottom < dropzoneBoundingBox.top ||
            draggableBoundingBox.top > dropzoneBoundingBox.bottom)

    }


}
