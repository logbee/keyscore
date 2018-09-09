import {
    AfterViewInit,
    Component, ComponentFactoryResolver,
    ComponentRef,
    ElementRef,
    HostListener, Inject,
    OnDestroy,
    OnInit,
    ViewChild,
    ViewContainerRef
} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DraggableModel} from "./models/draggable.model";
import {Subject} from "rxjs/index";
import {Draggable, Dropzone, Workspace} from "./models/contract";
import {DropzoneType} from "./models/dropzone-type";
import {Connection} from "./models/connection.model";
import {Rectangle} from "./models/rectangle";
import {DropzoneFactory} from "./dropzone/dropzone-factory";
import {DraggableFactory} from "./draggable/draggable-factory";
import {takeUntil} from "rxjs/internal/operators";


@Component({
    selector: "draggable",
    template: `
        <div #draggableElement [class]="'draggable flex-row'"
             [class.mirror]="draggableModel.isMirror"
             [class.d-flex]="visible"
             [class.d-none]="!visible"
             (mousedown)="dragStart($event)">
            <div class="connection previous-connection">
                <ng-template #previousConnection></ng-template>
            </div>
            <div class="blockContainer">
                <svg
                        xmlns="http://www.w3.org/2000/svg"
                        version="1.1"
                        viewBox="0 0 971 567.929"
                >
                    <svg:g default-connector-in [connectorIndicatorColor]="connectorIndicatorColorPrevious"></svg:g>
                    
                    <svg:g>
                        <svg:path d="M282.75 0.5 H 687.75 V 567.429 H 282.75 V 0.5"
                              id="rect5038"
                              style="fill:#365880;fill-opacity:1;stroke:#398033;stroke-width:0px"/>

                    </svg:g>
                    <svg:g default-connector-out [connectorIndicatorColor]="connectorIndicatorColorNext"></svg:g>
                </svg>
            </div>

            <div class="connection next-connection">
                <ng-template #nextConnection></ng-template>
            </div>

        </div>
    `
})

export class DraggableComponent implements OnInit, OnDestroy, Draggable, AfterViewInit {

    workspace: Workspace;
    draggableModel: DraggableModel;
    componentRef: ComponentRef<DraggableComponent>;

    @ViewChild("draggableElement") draggableElement: ElementRef;
    @ViewChild("previousConnection", {read: ViewContainerRef}) previousConnectionContainer: ViewContainerRef;
    @ViewChild("nextConnection", {read: ViewContainerRef}) nextConnectionContainer: ViewContainerRef;


    public id: string;

    private dragStartSource = new Subject<void>();
    private dragMoveSource = new Subject<void>();
    private isAlive = new Subject<void>();

    dragStart$ = this.dragStartSource.asObservable().pipe(takeUntil(this.isAlive));
    dragMove$ = this.dragMoveSource.asObservable().pipe(takeUntil(this.isAlive));

    private lastDragX: number;
    private lastDragY: number;
    private visible: boolean = true;
    private deleting: boolean = false;
    private preDragPosition: { x: number, y: number } = {x: 0, y: 0};

    private nextConnectionDropzone: Dropzone;
    private previousConnectionDropzone: Dropzone;

    private connectorIndicatorColorPrevious: string = "white";
    private connectorIndicatorColorNext: string = "white";

    private next: Draggable;

    private dropzoneFactory: DropzoneFactory;

    constructor(private resolver: ComponentFactoryResolver) {
        this.id = uuid();
        this.dropzoneFactory = new DropzoneFactory(resolver);
    }

    public ngOnInit() {

        this.positionDraggable();

        this.initialiseConnections();

        if (this.draggableModel.initialDropzone.getDropzoneModel().dropzoneType === DropzoneType.Connector) {
            this.occupyPreviousConnection();
        }

        if (this.draggableModel.next) {
            this.createNext();
        }
        if (this.draggableModel.draggableType === "delete") {
            this.triggerDelete();
        }

    }


    public ngAfterViewInit() {
        if (!this.getHead().getDraggableModel().isMirror) {
            this.workspace.registerDraggable(this);
        }
        else if (this.draggableModel.isMirror) {
            this.workspace.registerMirror(this);
        }
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

            this.draggableElement.nativeElement.style.left = newPosition.x + "px";
            this.draggableElement.nativeElement.style.top = newPosition.y + "px";

            this.triggerDragMove();
        }
    }

    private initialiseConnections() {
        if (this.draggableModel.rootDropzone === DropzoneType.Workspace) {
            this.previousConnectionDropzone =
                this.createConnection(this.draggableModel.previousConnection, this.previousConnectionContainer);
            this.nextConnectionDropzone =
                this.createConnection(this.draggableModel.nextConnection, this.nextConnectionContainer);
            if (this.previousConnectionDropzone) {
                this.previousConnectionDropzone.isDroppable$.subscribe(isDroppable =>
                    this.connectorIndicatorColorPrevious = isDroppable ? "lime" : "white"
                )
            }
            if (this.nextConnectionDropzone) {
                this.nextConnectionDropzone.isDroppable$.subscribe(isDroppable => {
                        this.connectorIndicatorColorNext = isDroppable ? "lime" : "white";
                    }
                )
            }
        }
    }

    private positionDraggable() {
        const hasAbsolutePosition: boolean = (this.draggableModel.isMirror ||
            this.draggableModel.initialDropzone.getDropzoneModel().dropzoneType !== DropzoneType.Toolbar);

        this.draggableElement.nativeElement.style.position = hasAbsolutePosition ? "absolute" : "relative";

        if (hasAbsolutePosition) {
            this.setPosition(this.draggableModel.position);
        }
    }

    private occupyPreviousConnection() {
        if (this.previousConnectionDropzone) {
            this.previousConnectionDropzone.occupyDropzone();
        }
    }

    private createConnection(connection: Connection, container: ViewContainerRef) {
        if (connection.isPermitted) {
            const connectionDropzone =
                this.dropzoneFactory.createConnectorDropzone(container, this.workspace, this, connection.connectableTypes);
            this.workspace.addDropzone(connectionDropzone);
            return connectionDropzone;
        }
    }

    public createNext() {
        const draggableFactory = new DraggableFactory(this.resolver);
        this.draggableModel.next =
            {...this.draggableModel.next, initialDropzone: this.nextConnectionDropzone, previous: this};
        this.next = draggableFactory.createDraggable(this.nextConnectionDropzone.getDraggableContainer(), this.draggableModel.next, this.workspace);
        this.nextConnectionDropzone.occupyDropzone();
    }

    public ngOnDestroy() {
        this.workspace.removeAllDropzones(dropzone => dropzone.getOwner() === this);
        this.workspace.removeDraggables(draggable => draggable.getId() === this.id);
        this.isAlive.next();
    }

    private triggerDragStart() {
        this.dragStartSource.next();
    }

    private triggerDragMove() {
        this.dragMoveSource.next();
    }

    private dragStart(event: MouseEvent) {
        event.stopPropagation();
        this.triggerDragStart();

    }

    getTail(): Draggable {
        let tail: Draggable = this;
        while (tail.getNext()) {
            tail = tail.getNext();
        }
        return tail;
    }

    getHead(): Draggable {
        let head: Draggable = this;
        while (head.getPrevious()) {
            head = head.getPrevious();
        }
        return head;
    }

    getTotalWidth(): number {
        let totalWidth = 0;
        let tail: Draggable = this;
        while (tail.getNext()) {
            totalWidth += tail.getDraggableSize().width;
            tail = tail.getNext();
        }

        return totalWidth;
    }

    moveXAxis(deltaX: number) {
        if (deltaX !== 0) {
            this.draggableModel.position.x += deltaX;
            this.draggableElement.nativeElement.style.left = this.draggableModel.position.x + "px";
        }
    }

    removeNextFromModel() {
        this.draggableModel.next = null;
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
        const clientRect: ClientRect = this.draggableElement.nativeElement.getBoundingClientRect();
        return {
            top: clientRect.top,
            left: clientRect.left,
            right: clientRect.right,
            bottom: clientRect.bottom,
            width: clientRect.width
        };
    }

    getDraggableModel(): DraggableModel {
        return this.draggableModel;
    }

    getId(): string {
        return this.id;
    }

    destroy(): void {
        this.componentRef.destroy();
    }

    hide(): void {
        this.preDragPosition = this.getDraggablePosition();
        this.visible = false;
    }

    show(): void {
        this.setPosition(this.preDragPosition);
        this.visible = true;
    }

    isVisible(): boolean {
        return this.visible;
    }

    getNextConnection(): Dropzone {
        return this.nextConnectionDropzone;
    }

    getNext(): Draggable {
        return this.next;
    }

    getPrevious(): Draggable {
        return this.draggableModel.previous;
    }


    getPreviousConnection(): Dropzone {
        return this.previousConnectionDropzone;
    }

    setNextModel(next: DraggableModel): void {
        this.draggableModel.next = next;
    }

    triggerDelete() {
        this.deleting = true;
        this.draggableElement.nativeElement.classList.add("delete");
        console.log(this.draggableElement.nativeElement.classList);
        this.draggableElement.nativeElement.addEventListener(this.whichTransitionEvent(), (e) => {
            this.destroy();
        }, false);
    }

    isDeleting(): boolean {
        return this.deleting;
    }

    private setPosition(pos: { x: number, y: number }) {

        this.draggableElement.nativeElement.style.left = pos.x + "px";
        this.draggableElement.nativeElement.style.top = pos.y + "px";
    }

    private whichTransitionEvent() {
        let t;
        const el = document.createElement('fakeelement');
        const transitions = {
            'transition': 'transitionend',
            'OTransition': 'oTransitionEnd',
            'MozTransition': 'transitionend',
            'WebkitTransition': 'webkitTransitionEnd'
        };

        for (t in transitions) {
            if (el.style[t] !== undefined) {
                return transitions[t];
            }
        }
    }
}
