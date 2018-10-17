import {
    Component,
    ElementRef,
    EventEmitter,
    HostListener,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    ViewChild,
    ViewContainerRef
} from "@angular/core";
import {v4 as uuid} from "uuid";
import {DRAGGABLE_HEIGHT, DRAGGABLE_WIDTH, DraggableModel} from "./models/draggable.model";
import {Draggable, Dropzone, Workspace} from "./models/contract";
import {DropzoneType} from "./models/dropzone-type";
import {DropzoneFactory} from "./dropzone/dropzone-factory";
import {DraggableFactory} from "./draggable/draggable-factory";
import {computeRelativePositionToParent} from "./util/util";
import {WorkspaceDropzoneSubcomponent} from "./dropzone/workspace-dropzone-subcomponent";
import {BlockDescriptor} from "./models/block-descriptor.model";
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {share, takeUntil} from "rxjs/operators";
import {EditingPipelineModel} from "../../../models/pipeline-model/EditingPipelineModel";
import "./style/pipely-style.scss";
import {PipelineConfiguratorService} from "./services/pipeline-configurator.service";
import {
    Blueprint,
    BlueprintJsonClass,
    FilterBlueprint,
    SinkBlueprint,
    SourceBlueprint
} from "../../../models/blueprints/Blueprint";
import {DraggableComponent} from "./draggable.component";
import {Configuration} from "../../../models/common/Configuration";


@Component({
    selector: "pipely-workspace",
    template: `
        <div class="pipely-wrapper">
            <div class="workspace-container" fxLayout="row" fxFill>
                <div #workspace class="workspace" fxFlex="75">
                    <div class="row">
                        <ng-template #workspaceContainer>
                        </ng-template>
                        <puzzle-box class="top-shadow" [workspace]="this"
                                    [descriptors]="blockDescriptors$|async"></puzzle-box>
                    </div>
                </div>

                <configurator class="mat-elevation-z8" fxFlex="" (closeConfigurator)="closeConfigurator()"
                              [isOpened]="isConfiguratorOpened"
                              [selectedBlock]="{configuration:(selectedDraggable$|async)?.getDraggableModel().configuration,
                              descriptor:(selectedDraggable$|async)?.getDraggableModel().blockDescriptor}"
                              (onSave)="saveConfiguration($event)">
                </configurator>
            </div>
        </div>
    `
})


export class WorkspaceComponent implements OnInit, OnDestroy, OnChanges, Workspace {
    @Input() pipeline: EditingPipelineModel;

    @Input('blockDescriptors') set blockDescriptors(descriptors: BlockDescriptor[]) {
        this.blockDescriptors$.next(descriptors)
    };

    private blockDescriptors$ = new BehaviorSubject<BlockDescriptor[]>([]);

    @Input() runTrigger$: Observable<void>;
    @Input() saveTrigger$: Observable<void>;


    @ViewChild("workspaceContainer", {read: ViewContainerRef}) workspaceContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ViewContainerRef}) mirrorContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ElementRef}) workspaceElement: ElementRef;

    @Output() onUpdatePipeline: EventEmitter<EditingPipelineModel> = new EventEmitter();
    @Output() onRunPipeline: EventEmitter<EditingPipelineModel> = new EventEmitter();

    public id: string;

    public dropzones: Set<Dropzone> = new Set();
    public draggables: Draggable[] = [];

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

    private isAlive$: Subject<void> = new Subject<void>();

    constructor(private dropzoneFactory: DropzoneFactory,
                private draggableFactory: DraggableFactory,
                private pipelineConfigurator: PipelineConfiguratorService) {
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

    private closeConfigurator() {
        this.isConfiguratorOpened = false;
    }

    private saveConfiguration(configuration: Configuration) {
        this.selectedDraggable.getDraggableModel().configuration = configuration;
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

        this.dropzones.add(this.workspaceDropzone);
        this.dropzones.add(this.dropzoneFactory.createTrashDropzone(this.workspaceContainer, this));

        this.saveTrigger$.pipe(takeUntil(this.isAlive$)).subscribe(() => {
                this.pipeline = this.pipelineConfigurator.updatePipelineModel(this.draggables, this.pipeline);
                this.onUpdatePipeline.emit(this.pipeline);
            }
        );

        this.runTrigger$.pipe(takeUntil(this.isAlive$)).subscribe(() => {
            console.log("Trigger!");
            this.pipeline = this.pipelineConfigurator.updatePipelineModel(this.draggables,this.pipeline);
            this.onRunPipeline.emit(this.pipeline);
        });

        this.buildEditPipeline();

    }

    ngOnChanges(changes) {
        if (changes['pipeline']) {
        }
    }


    private buildEditPipeline() {
        let nextBlueprint: Blueprint = this.pipeline.blueprints.find(blueprint =>
            blueprint.jsonClass === BlueprintJsonClass.SinkBlueprint);

        if (!nextBlueprint) return;
        //TODO find possibility to center (workspace element width is 0?!) without this magic number shit (0.75 => percentage of workspace width,250px => width of sidebar)
        const dropzone = this.workspaceDropzone.getSubComponent() as WorkspaceDropzoneSubcomponent;
        let sourceXPosition = (this.workspaceElement.nativeElement.offsetWidth * 0.75 + 250) / 2 - ((DRAGGABLE_WIDTH * this.pipeline.blueprints.length) / 2);
        let sourceYPosition = dropzone.workspaceScrollContainer.nativeElement.offsetHeight / 2 - DRAGGABLE_HEIGHT / 2;

        let models: DraggableModel[] = [];
        for (let i = 0; i < this.pipeline.blueprints.length; i++) {
            let conf = this.pipeline.configurations.find(conf => conf.ref.uuid === nextBlueprint.configuration.uuid);
            let descriptor = this.blockDescriptors$.getValue().find(descriptor => descriptor.ref.uuid === nextBlueprint.descriptor.uuid);
            models.push({
                blockDescriptor: descriptor,
                configuration: {
                    ref: nextBlueprint.configuration,
                    parent: null,
                    parameters: conf.parameters
                },
                blueprintRef: nextBlueprint.ref,
                initialDropzone: this.workspaceDropzone,
                next: null,
                previous: null,
                rootDropzone: DropzoneType.Workspace,
                isMirror: false,
                position: {x: sourceXPosition, y: sourceYPosition}
            });
            if (i > 0) {
                models[i].next = models[i - 1]
            }


            if (nextBlueprint.jsonClass !== BlueprintJsonClass.SourceBlueprint) {
                nextBlueprint = this.pipeline.blueprints.find(blueprint => blueprint.ref.uuid === (nextBlueprint as FilterBlueprint | SinkBlueprint).in.uuid);
            } else {
                break;
            }
        }
        this.draggableFactory.createDraggable(this.workspaceDropzone.getDraggableContainer(), models[models.length - 1], this);

    }

    ngOnDestroy() {
        this.isAlive$.next();
        this.isAlive$.complete();
    }
}


