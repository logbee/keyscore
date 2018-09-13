import {
    AfterViewInit, Component, ElementRef, HostListener, Input, OnDestroy, OnInit, ViewChild,
    ViewContainerRef
} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DraggableModel} from "./models/draggable.model";
import {Draggable, Dropzone, Workspace} from "./models/contract";
import {DropzoneType} from "./models/dropzone-type";
import {DropzoneFactory} from "./dropzone/dropzone-factory";
import {DraggableFactory} from "./draggable/draggable-factory";
import {computeRelativePositionToParent} from "./util/util";
import {WorkspaceDropzoneSubcomponent} from "./dropzone/workspace-dropzone-subcomponent";
import {BlockDescriptor} from "./models/block-descriptor.model";
import {InternalPipelineConfiguration} from "../../../models/pipeline-model/InternalPipelineConfiguration";
import {PipelyPipelineConfiguration} from "./models/pipeline-configuration.model";
import {parameterDescriptorToParameter} from "../../../util";
import {Observable, Subject} from "rxjs";
import {share} from "rxjs/operators";

@Component({
    selector: "workspace",
    template: `
        <mat-sidenav-container class="workspace-container">
            <mat-sidenav configurator class="configurator" #sidenav #right position="end" mode="over"
                         [(opened)]="isConfiguratorOpened">
                <configurator (closeConfigurator)="closeConfigurator()" [isOpened]="isConfiguratorOpened"
                              [selectedDraggable$]="selectedDraggable$"></configurator>
            </mat-sidenav>

            <mat-sidenav-content>
                <div #workspace class="workspace">
                    <div class="row">
                        <ng-template #toolbarContainer></ng-template>
                        <ng-template #workspaceContainer>
                        </ng-template>
                    </div>
                </div>
            </mat-sidenav-content>
        </mat-sidenav-container>
    `
})


export class WorkspaceComponent implements OnInit, OnDestroy, Workspace, AfterViewInit {
    @Input() pipeline: PipelyPipelineConfiguration;

    @ViewChild("workspaceContainer", {read: ViewContainerRef}) workspaceContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ViewContainerRef}) mirrorContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ElementRef}) workspaceElement: ElementRef;
    @ViewChild("toolbarContainer", {read: ViewContainerRef}) toolbarContainer: ViewContainerRef;


    public id: string;

    public dropzones: Set<Dropzone> = new Set();
    public draggables: Draggable[] = [];

    public toolbarDropzone: Dropzone;
    public workspaceDropzone: Dropzone;

    private isDragging: boolean = false;
    private bestDropzone: Dropzone;

    private draggedDraggable: Draggable;
    private mirrorDraggable: Draggable;

    private selectedDraggableSource: Subject<Draggable> = new Subject();
    private selectedDraggable$: Observable<Draggable> = this.selectedDraggableSource.asObservable().pipe(share());
    private selectedDraggable: Draggable;

    private mouseDownStart: { x: number, y: number } = {x: -1, y: -1};

    private isConfiguratorOpened: boolean = false;

    constructor(private dropzoneFactory: DropzoneFactory,
                private draggableFactory: DraggableFactory) {
        this.id = uuid();
    }


    private draggableMouseDown(draggable: Draggable, event: MouseEvent) {
        this.mouseDownStart = {x: event.clientX, y: event.clientY};
        this.selectedDraggable = draggable;
    }

    @HostListener('mousemove', ['$event'])
    private mouseMove(event: MouseEvent) {
        if (this.isMouseDown() && !this.isDragging && this.movedOverTolerance({
            x: event.clientX,
            y: event.clientY
        })) {
            this.startDragging();
        }

        if (this.isDragging) {
            this.moveMirror(event);
            this.checkForPossibleDropzone();
        }
    }


    @HostListener('mouseup', ['$event'])
    private mouseUp(event: MouseEvent) {

        if (this.isMouseDown() && !this.movedOverTolerance({x: event.clientX, y: event.clientY})) {
            this.click(event);
        }
        else if (this.isDragging) {
            this.stopDragging();
        }

        this.mouseDownStart = {x: -1, y: -1};
    }

    private click(event: MouseEvent) {
        if (this.selectedDraggable.getDraggableModel().rootDropzone === DropzoneType.Workspace) {
            this.isConfiguratorOpened = true;
            this.selectedDraggableSource.next(this.selectedDraggable);

        }
    }

    private movedOverTolerance(currentPosition: { x: number, y: number }): boolean {
        const moveTolerance = 5;
        return currentPosition.x < this.mouseDownStart.x - moveTolerance ||
            currentPosition.x > this.mouseDownStart.x + moveTolerance ||
            currentPosition.y < this.mouseDownStart.y - moveTolerance ||
            currentPosition.y > this.mouseDownStart.y + moveTolerance
    }

    private checkForPossibleDropzone() {
        let lastDropzone: Dropzone = null;
        this.dropzones.forEach(dropzone => {
            lastDropzone = dropzone.computeBestDropzone(this.mirrorDraggable, lastDropzone);
        });
        if (this.bestDropzone) {
            this.bestDropzone.setIsDroppable(false);
        }
        if (lastDropzone) {
            lastDropzone.setIsDroppable(true);
        }
        this.bestDropzone = lastDropzone;
    }

    private isMouseDown() {
        return this.mouseDownStart.x !== -1 && this.mouseDownStart.y !== -1;
    }

    private startDragging() {
        this.isDragging = true;
        this.draggedDraggable = this.selectedDraggable;
        const mirrorModel = this.initialiseMirrorComponent();
        this.createMirror(mirrorModel);

        if (this.draggedDraggable.getDraggableModel().rootDropzone === DropzoneType.Workspace) {
            this.draggedDraggable.hide();
        }
    }

    private stopDragging() {
        if (this.bestDropzone) {
            this.bestDropzone.drop(this.mirrorDraggable, this.draggedDraggable);
        }
        if (this.draggedDraggable && !this.draggedDraggable.isVisible()) {
            this.draggedDraggable.show();
        }

        this.mirrorDraggable.destroy();

        this.isDragging = false;
        this.bestDropzone = null;
    }

    private moveMirror(event: MouseEvent) {
        const lastDrag: { x: number, y: number } = this.mirrorDraggable.getLastDrag();
        const deltaX = event.clientX - lastDrag.x;
        const deltaY = event.clientY - lastDrag.y;
        this.mirrorDraggable.setLastDrag(event.clientX, event.clientY);
        this.mirrorDraggable.moveMirror(deltaX, deltaY);
    }


    private initialiseMirrorComponent(): DraggableModel {
        const workspaceRect = this.workspaceElement.nativeElement.getBoundingClientRect();
        const scrollContainer: ElementRef =
            (this.workspaceDropzone.getSubComponent() as WorkspaceDropzoneSubcomponent)
                .workspaceScrollContainer;
        let draggedPos = this.draggedDraggable.getAbsoluteDraggablePosition();

        const absolutePos = {x: draggedPos.x + scrollContainer.nativeElement.scrollLeft, y: draggedPos.y};
        const relativeMirrorPosition = computeRelativePositionToParent(
            absolutePos,
            {x: workspaceRect.left, y: workspaceRect.top});

        return {
            ...this.draggedDraggable.getDraggableModel(),
            isMirror: true,
            position: relativeMirrorPosition,
            previous: null
        };
    }


    private createMirror(model: DraggableModel) {
        this.mirrorDraggable = this.draggableFactory.createDraggable(this.workspaceDropzone.getDraggableContainer(), model, this);
    }

    private computeWorkspaceSize() {
        const compResult =
            (this.workspaceDropzone.getSubComponent() as WorkspaceDropzoneSubcomponent)
                .resizeWorkspaceOnDrop(this.draggables);

        this.draggables.forEach(draggable => draggable.moveXAxis(compResult));
    }

    private closeConfigurator(){
        this.isConfiguratorOpened = false;
    }

    addDropzone(dropzone: Dropzone) {
        this.dropzones.add(dropzone);
    }

    removeAllDropzones(predicate: (dropzone: Dropzone) => boolean) {
        this.dropzones.forEach(dropzone => {
            if (predicate(dropzone)) {
                this.dropzones.delete(dropzone);
            }
        });
    }

    removeDraggables(predicate: (draggable: Draggable) => boolean) {
        this.draggables.forEach((draggable, index, array) => {
            if (predicate(draggable)) {
                array.splice(index, 1);
            }
        });
    }

    registerDraggable(draggable: Draggable) {
        draggable.dragStart$.subscribe((event) => this.draggableMouseDown(draggable, event));
        if (draggable.getDraggableModel().initialDropzone
            .getDropzoneModel().dropzoneType === DropzoneType.Workspace) {
            this.draggables.push(draggable);
        }
        if (draggable.getDraggableModel().initialDropzone.getDropzoneModel().dropzoneType !==
            DropzoneType.Toolbar) {
            this.computeWorkspaceSize();
        }
    }

    getWorkspaceDropzone(): Dropzone {
        return this.workspaceDropzone;
    }

    ngOnInit() {
        this.workspaceDropzone = this.dropzoneFactory.createWorkspaceDropzone(this.workspaceContainer, this);
        this.toolbarDropzone = this.dropzoneFactory.createToolbarDropzone(this.toolbarContainer, this);

        this.dropzones.add(this.workspaceDropzone);
        this.dropzones.add(this.dropzoneFactory.createTrashDropzone(this.workspaceContainer, this));

        let inType: string = "no-connection-in";
        let outType: string = "default-out";
        for (let i = 0; i < 3; i++) {

            if (i === 1) {
                inType = "default-in";
            } else if (i == 2) {
                outType = "no-connection-out"
            }
            let blockDescriptor = this.createDummyBlockDescriptor(inType, outType);
            let parameters = blockDescriptor.parameters.map(parameterDescriptor => parameterDescriptorToParameter(parameterDescriptor));
            let blockConfiguration = {
                id: uuid(),
                descriptor: blockDescriptor,
                parameters: parameters
            };
            this.draggableFactory.createDraggable(this.toolbarDropzone.getDraggableContainer(), {
                blockDescriptor: blockDescriptor,
                blockConfiguration: blockConfiguration,
                initialDropzone: this.toolbarDropzone,
                next: null,
                previous: null,
                rootDropzone: this.toolbarDropzone.getDropzoneModel().dropzoneType,
                isMirror: false
            }, this);
        }

    }

    private createDummyBlockDescriptor(inType: string, outType: string): BlockDescriptor {
        let name = Math.random().toString().substr(0, 4);
        return {
            name: name,
            displayName: "Block " + name,
            description: "Ipsum lorem, bla bla bla!",
            previousConnection: {
                isPermitted: true,
                connectableTypes: inType !== "no-connection-in" ? ["default-out"] : [],
                connectionType: inType
            },
            nextConnection: {
                isPermitted: true,
                connectableTypes: outType !== "no-connection-out" ? ["default-in"] : [],
                connectionType: outType
            },
            parameters: [
                {
                    name: "TestFeld1",
                    displayName: "Text Parameter",
                    jsonClass: "TextParameterDescriptor",
                    mandatory: true
                },
                {
                    name: "TestList1",
                    displayName: "Liste ",
                    jsonClass: "ListParameterDescriptor",
                    mandatory: true
                },
                {
                    name: "TestFeld3",
                    displayName: "Int Parameter",
                    jsonClass: "IntParameterDescriptor",
                    mandatory: true
                },
                {
                    name: "TestFeld4",
                    displayName: "Boolean Parameter",
                    jsonClass: "BooleanParameterDescriptor",
                    mandatory: true
                }
            ],
            category: "Test"
        };


    }

    ngAfterViewInit() {

    }

    ngOnDestroy() {

    }

}
