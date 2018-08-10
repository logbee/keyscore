import {Component, ElementRef, HostBinding, HostListener, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {WorkspaceComponent} from "./workspace.component";
import {v4 as uuid} from "uuid";
import {DragService} from "./services/drag.service";
import {DragMoveEvent} from "./events/drag-move.event";
import {DraggableModel} from "./models/draggable.model";
import {DragStartEvent} from "./events/drag-start.event";


@Component({
    selector: "draggable",
    template: `
        <div #draggableElement [class]="'draggable'" [class.mirror]="draggableModel.isMirror" [class.hide]="isHidden"
             (mousedown)="dragStart($event)"
             (mouseup)="dragStop($event)">{{draggableModel.name}}
        </div>
    `
})


export class DraggableComponent implements OnInit, OnDestroy {

    @Input() workspace: WorkspaceComponent;
    @Input() draggableModel: DraggableModel;

    @ViewChild("draggableElement") draggableElement: ElementRef;

    public id: string;

    private isHidden: boolean = false;
    private lastDragX: number;
    private lastDragY: number;

    private preDragPosition: { x: number, y: number } = {x: 0, y: 0};


    @HostListener('document:mousemove', ['$event'])
    onMouseMove(event: MouseEvent) {
        if (this.draggableModel.isMirror) {
            const x = event.clientX - this.lastDragX;
            const y = event.clientY - this.lastDragY;
            this.lastDragX = event.clientX;
            this.lastDragY = event.clientY;
            const currentPosition = this.getDraggablePosition();
            const newPosition = {
                x: currentPosition.x + x,
                y: currentPosition.y + y
            };
            const size = this.getDraggableSize();

            this.dragService.triggerDragMove(new DragMoveEvent(newPosition, size, this.draggableModel.dropzoneType));

            this.draggableElement.nativeElement.style.left = newPosition.x + "px";
            this.draggableElement.nativeElement.style.top = newPosition.y + "px";

        }
    }

    @HostListener('document:mouseup', ['$event'])
    onMouseUp(event: MouseEvent) {
        //this.isDragged = false;
        this.draggableElement.nativeElement.style.top = this.preDragPosition.y + "px";
        this.draggableElement.nativeElement.style.left = this.preDragPosition.x + "px";
        //this.draggableElement.nativeElement.style.position = "relative";

    }

    constructor(private dragService: DragService) {
        this.id = uuid();
    }

    public ngOnInit() {

    }

    public ngOnDestroy() {

    }

    private dragStop(event: MouseEvent) {
        //this.isDragged = false;
        //this.draggableElement.nativeElement.style.position = "relative";

    }

    private dragStart(event: MouseEvent) {
        console.log(event);
        this.preDragPosition.x = this.draggableElement.nativeElement.offsetLeft;
        this.preDragPosition.y = this.draggableElement.nativeElement.offsetTop;
        this.lastDragX = event.clientX;
        this.lastDragY = event.clientY;
        let eventDraggableModel = {
            ...this.draggableModel,
            hasAbsolutePosition: true,
            isMirror: true,
            draggablePosition: this.getDraggablePosition(),
            draggableSize: this.getDraggableSize()

        };
        this.dragService.triggerDragStart(new DragStartEvent(eventDraggableModel,event))


    }

    private getDraggablePosition(): { x: number, y: number } {
        return {
            x: this.draggableElement.nativeElement.offsetLeft,
            y: this.draggableElement.nativeElement.offsetTop
        };
    }

    private getDraggableSize(): { width: number, height: number } {
        return {
            width: this.draggableElement.nativeElement.offsetWidth,
            height: this.draggableElement.nativeElement.offsetHeight
        }
    }
}
