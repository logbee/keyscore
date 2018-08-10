import {Component, ElementRef, HostListener, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DraggableModel} from "./models/draggable.model";
import {Subject} from "rxjs/index";
import {Draggable, Dropzone, Workspace} from "./models/contract";


@Component({
    selector: "draggable",
    template: `
        <div #draggableElement [class]="'draggable'" [class.mirror]="draggableModel.isMirror"
             (mousedown)="dragStart($event)"
             (mouseup)="dragStop($event)">{{draggableModel.name}}
        </div>
    `
})

export class DraggableComponent implements OnInit, OnDestroy, Draggable {

    workspace: Workspace;
    draggableModel: DraggableModel;

    @ViewChild("draggableElement") draggableElement: ElementRef;

    public id: string;

    private dragStartSource = new Subject<void>();
    private dragMoveSource = new Subject<void>();

    dragStart$ = this.dragStartSource.asObservable();
    dragMove$ = this.dragMoveSource.asObservable();

    private lastDragX: number;
    private lastDragY: number;

    private preDragPosition: { x: number, y: number } = {x: 0, y: 0};


    constructor() {
        this.id = uuid();
    }

    public ngOnInit() {
        if (this.draggableModel.hasAbsolutePosition) {
            this.draggableElement.nativeElement.style.position = "absolute";
        }
        if (this.draggableModel.position && this.draggableModel.hasAbsolutePosition) {
            this.draggableElement.nativeElement.style.left = this.draggableModel.position.x + "px";
            this.draggableElement.nativeElement.style.top = this.draggableModel.position.y + "px";
        }
    }

    public ngOnDestroy() {

    }

    @HostListener('document:mousemove', ['$event'])
    onMouseMove(event: MouseEvent) {
        if (
            this.draggableModel.isMirror ||
            this.draggableModel.initialDropzone.getDropzoneModel().dropzoneType ===
            "workspace") {

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

            this.triggerDragMove();

            this.draggableElement.nativeElement.style.left = newPosition.x + "px";
            this.draggableElement.nativeElement.style.top = newPosition.y + "px";

        }
    }

    private triggerDragStart() {
        this.dragStartSource.next();
    }

    private triggerDragMove() {
        this.dragMoveSource.next();
    }

    private dragStart(event: MouseEvent) {
        console.log(event);
        this.lastDragX = event.clientX;
        this.lastDragY = event.clientY;

        this.triggerDragStart();


    }

    getDraggablePosition(): { x: number, y: number } {
        return {
            x: this.draggableElement.nativeElement.offsetLeft,
            y: this.draggableElement.nativeElement.offsetTop
        };
    }

    getAbsoluteDraggablePosition(): { x: number, y: number } {
        return {
            x: this.draggableElement.nativeElement.getBoundingClientRect().left,
            y: this.draggableElement.nativeElement.getBoundingClientRect().top

        }
    }

    getDraggableSize(): { width: number, height: number } {
        return {
            width: this.draggableElement.nativeElement.offsetWidth,
            height: this.draggableElement.nativeElement.offsetHeight
        }
    }

    getDraggableModel(): DraggableModel {
        return this.draggableModel;
    }

    getId(): string {
        return this.id;
    }
}
