import {
    Component, ComponentFactoryResolver, ComponentRef, ElementRef, HostListener, Input, OnDestroy, OnInit,
    ViewChild, ViewContainerRef
} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DraggableModel} from "./models/draggable.model";
import {Subject} from "rxjs/index";
import {Draggable, Dropzone, Workspace} from "./models/contract";
import {DropzoneType} from "./models/dropzone-type";
import {DropzoneComponent} from "./dropzone.component";
import {Connection} from "./models/connection.model";
import {Rectangle} from "./models/rectangle";


@Component({
    selector: "draggable",
    template: `
        <div #draggableElement [class]="'draggable'" [class.mirror]="draggableModel.isMirror"
             (mousedown)="dragStart($event)">
            <div class="connection previous-connection">
                <ng-template #previousConnection></ng-template>
            </div>
            {{draggableModel.name}}
            <div class="connection next-connection">
                <ng-template #nextConnection></ng-template>
            </div>

        </div>
    `
})

export class DraggableComponent implements OnInit, OnDestroy, Draggable {

    workspace: Workspace;
    draggableModel: DraggableModel;
    componentRef: ComponentRef<DraggableComponent>;

    @ViewChild("draggableElement") draggableElement: ElementRef;
    @ViewChild("previousConnection", {read: ViewContainerRef}) previousConnectionContainer: ViewContainerRef;
    @ViewChild("nextConnection", {read: ViewContainerRef}) nextConnectionContainer: ViewContainerRef;


    public id: string;

    private dragStartSource = new Subject<void>();
    private dragMoveSource = new Subject<void>();

    dragStart$ = this.dragStartSource.asObservable();
    dragMove$ = this.dragMoveSource.asObservable();

    private lastDragX: number;
    private lastDragY: number;
    private visible: boolean = true;

    private preDragPosition: { x: number, y: number } = {x: 0, y: 0};

    private nextConnection: Dropzone;
    private previousConnection: Dropzone;

    private next: Draggable;

    constructor(private resolver: ComponentFactoryResolver) {
        this.id = uuid();
    }

    public ngOnInit() {
        if (this.draggableModel.hasAbsolutePosition) {
            this.draggableElement.nativeElement.style.position = "absolute";
        }
        if (this.draggableModel.position && this.draggableModel.hasAbsolutePosition) {
            this.setPosition(this.draggableModel.position)
        }

        if (this.draggableModel.rootDropzone === DropzoneType.Workspace) {
            this.previousConnection = this.createConnection(this.draggableModel.previousConnection, this.previousConnectionContainer);
            this.nextConnection = this.createConnection(this.draggableModel.nextConnection, this.nextConnectionContainer);
        }

        if (this.draggableModel.parent) {
            if (this.previousConnection) {
                this.previousConnection.occupyDropzone();
            }
            this.draggableModel.parent.getDraggableModel().next = this.draggableModel;
        }

        if (this.draggableModel.next) {
            this.next = this.workspace.createDraggableComponent(this.nextConnection, this.draggableModel.next);
        }
    }


    private createConnection(connection: Connection, container: ViewContainerRef) {
        if (connection.isPermitted) {
            const dropzoneFactory = this.resolver.resolveComponentFactory(DropzoneComponent);
            const dropzoneRef = container.createComponent(dropzoneFactory);
            const connectionDropzone: Dropzone = dropzoneRef.instance;
            dropzoneRef.instance.workspace = this.workspace;
            dropzoneRef.instance.dropzoneModel = {
                dropzoneType: DropzoneType.Connector,
                acceptedDraggableTypes: connection.connectableTypes,
                dropzoneRadius: 30
            };
            dropzoneRef.instance.owner = this;
            this.workspace.addDropzone(connectionDropzone);
            console.log("Connection created");

            return connectionDropzone;


        }
    }

    public ngOnDestroy() {
        this.workspace.removeAllDropzones(dropzone => dropzone.getOwner() === this);
    }

    @HostListener('document:mousemove', ['$event'])
    onMouseMove(event: MouseEvent) {
        if (
            this.draggableModel.isMirror) {

            const x = event.clientX - this.lastDragX;
            const y = event.clientY - this.lastDragY;
            this.lastDragX = event.clientX;
            this.lastDragY = event.clientY;
            const currentPosition = this.getDraggablePosition();
            const newPosition = {
                x: currentPosition.x + x,
                y: currentPosition.y + y
            };

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
        event.stopPropagation();

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

    getRectangle(): Rectangle {
        const position = this.getAbsoluteDraggablePosition();
        const size = this.getDraggableSize();
        return {
            top: position.y,
            left: position.x,
            right: position.x + size.width,
            bottom: position.y + size.height
        };
    }

    getDraggableModel(): DraggableModel {
        return this.draggableModel;
    }

    getId(): string {
        return this.id;
    }

    destroy(): void {
        if (this.getDraggableModel().parent) {
            this.getDraggableModel().parent.getDraggableModel().next = null;
            console.log("Parent after destroy", this.getDraggableModel().parent);
        }
        this.componentRef.destroy();
    }

    hide(): void {
        this.preDragPosition = this.getDraggablePosition();
        this.draggableElement.nativeElement.style.display = "none";
        this.visible = false;
    }

    show(): void {
        this.draggableElement.nativeElement.style.display = "block";
        this.setPosition(this.preDragPosition);
        this.visible = true;
    }

    isVisible(): boolean {
        return this.visible;
    }

    getNextConnection(): Dropzone {
        return this.nextConnection;
    }

    getNext(): Draggable {
        return this.next;
    }

    getPreviousConnection(): Dropzone {
        return this.previousConnection;
    }

    private setPosition(pos: { x: number, y: number }) {

        this.draggableElement.nativeElement.style.left = pos.x + "px";
        this.draggableElement.nativeElement.style.top = pos.y + "px";
    }
}
