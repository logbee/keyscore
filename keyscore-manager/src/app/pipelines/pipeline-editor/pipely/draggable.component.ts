import {Component, ElementRef, HostListener, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {WorkspaceComponent} from "./workspace.component";

@Component({
    selector: "draggable",
    template: `
        <div #draggableElement  [class]="isDragged ? 'draggable mirror' : 'draggable'" (mousedown)="dragStart($event)" (mouseup)="dragStop($event)">Test</div>
    `
})



export class DraggableComponent implements OnInit, OnDestroy {

    @Input() workspace:WorkspaceComponent;

    @ViewChild("draggableElement") draggableElement: ElementRef;

    private isDragged: boolean = false;
    private lastDragX: number;
    private lastDragY: number;



    @HostListener('document:mousemove', ['$event'])
    onMouseMove(event: MouseEvent) {
        if (this.isDragged) {
            console.log(event);
            const x = event.clientX - this.lastDragX;
            const y = event.clientY - this.lastDragY;
            this.lastDragX = event.clientX;
            this.lastDragY = event.clientY;
            this.draggableElement.nativeElement.style.left = (this.draggableElement.nativeElement.offsetLeft + x) + "px";
            this.draggableElement.nativeElement.style.top = (this.draggableElement.nativeElement.offsetTop + y) + "px";

        }
    }

    @HostListener('document:mouseup', ['$event'])
    onMouseUp(event: MouseEvent) {
        this.isDragged = false;
        //this.draggableElement.nativeElement.style.position = "relative";

    }

    constructor() {

    }

    public ngOnInit() {

    }

    public ngOnDestroy() {

    }

    private dragStop(event: MouseEvent) {
        this.isDragged = false;
        //this.draggableElement.nativeElement.style.position = "relative";

    }

    private dragStart(event: MouseEvent) {
        console.log(event);
        this.lastDragX = event.clientX;
        this.lastDragY = event.clientY;
        this.isDragged = true;
        this.draggableElement.nativeElement.style.position = "absolute";


    }
}
