import {
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    ElementRef,
    EventEmitter,
    HostListener,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    Renderer2,
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
import {Blueprint, BlueprintJsonClass, FilterBlueprint, SinkBlueprint} from "../../../models/blueprints/Blueprint";
import {Configuration} from "../../../models/common/Configuration";
import {TextValue} from "../../../models/dataset/Value";


@Component({
    selector: "pipely-workspace",
    template: `
        <div class="pipely-wrapper">
            <div class="workspace-container" fxLayout="row" fxFill>
                <div #workspace class="workspace" fxFlex="75">
                    <div class="row">
                        <ng-template #workspaceContainer>
                        </ng-template>
                        <puzzle-box *ngIf="!isInspecting; else datasetTable" class="top-shadow" [workspace]="this"
                                    [descriptors]="blockDescriptors$|async"></puzzle-box>
                        <ng-template #datasetTable>
                            <data-table [selectedBlock]="blockToDisplayDataset()" class="top-shadow"></data-table>
                        </ng-template>

                    </div>
                </div>

                <configurator class="mat-elevation-z8" fxFlex=""
                              [pipelineMetaData]="pipelineMetaData"
                              [selectedBlock]="{configuration:(selectedDraggable$|async)?.getDraggableModel().configuration,
                              descriptor:(selectedDraggable$|async)?.getDraggableModel().blockDescriptor}"
                              (onSave)="saveConfiguration($event)"
                              (onSavePipelineMetaData)="savePipelineMetaData($event)">
                </configurator>
            </div>
        </div>
    `
})


export class WorkspaceComponent implements OnInit, OnDestroy, AfterViewInit, OnChanges, Workspace {
    @Input() pipeline: EditingPipelineModel;

    @Input('blockDescriptors') set blockDescriptors(descriptors: BlockDescriptor[]) {
        this.blockDescriptors$.next(descriptors)
    };

    private blockDescriptors$ = new BehaviorSubject<BlockDescriptor[]>([]);

    private isInspecting: boolean = false;

    @Input() runTrigger$: Observable<void>;
    @Input() saveTrigger$: Observable<void>;
    @Input() inspectTrigger$: Observable<void>;


    @ViewChild("workspaceContainer", {read: ViewContainerRef}) workspaceContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ViewContainerRef}) mirrorContainer: ViewContainerRef;
    @ViewChild("workspace", {read: ElementRef}) workspaceElement: ElementRef;

    @Output() onUpdatePipeline: EventEmitter<EditingPipelineModel> = new EventEmitter();
    @Output() onRunPipeline: EventEmitter<EditingPipelineModel> = new EventEmitter();
    @Output() onSelectBlock: EventEmitter<DraggableModel> = new EventEmitter();

    public id: string;

    public dropzones: Set<Dropzone> = new Set();
    public draggables: Draggable[] = [];

    public workspaceDropzone: Dropzone;

    private isDragging: boolean = false;
    private bestDropzone: Dropzone;

    private draggedDraggable: Draggable;
    private mirrorDraggable: Draggable;

    private selectedDraggableSource: BehaviorSubject<Draggable> = new BehaviorSubject(null);
    private selectedDraggable$: Observable<Draggable> = this.selectedDraggableSource.asObservable().pipe(share());
    private selectedDraggable: Draggable;
    private pipelineMetaData: { name: string, description: string };

    private mouseDownStart: { x: number, y: number } = {x: -1, y: -1};

    private isConfiguratorOpened: boolean = false;

    private isAlive$: Subject<void> = new Subject<void>();

    private isDraggableMouseDown: boolean;

    constructor(private dropzoneFactory: DropzoneFactory,
                private draggableFactory: DraggableFactory,
                private pipelineConfigurator: PipelineConfiguratorService,
                private cd: ChangeDetectorRef,
                private renderer: Renderer2) {
        this.id = uuid();
    }

    private draggableMouseDown(draggable: Draggable, event: MouseEvent) {
        this.isDraggableMouseDown = true;
        this.mouseDownStart = {x: event.clientX, y: event.clientY};
        if (this.selectedDraggable) {
            this.selectedDraggable.select(false);
        }
        this.selectedDraggable = draggable;
        if (this.selectedDraggable.getDraggableModel().initialDropzone.getDropzoneModel().dropzoneType !== DropzoneType.Toolbar) {
            this.selectBlock(this.selectedDraggable);
            this.selectedDraggable.select(true);
        }

    }

    private blockToDisplayDataset() {
        let sink = this.pipeline.blueprints.find(blueprint => blueprint.jsonClass === BlueprintJsonClass.SinkBlueprint);
        return this.selectedDraggableSource.getValue() ? this.selectedDraggableSource.getValue().getDraggableModel().blueprintRef.uuid : sink.ref.uuid;

    }

    private selectBlock(selected: Draggable) {
        this.selectedDraggableSource.next(selected);
        if (selected) {
            this.onSelectBlock.emit(selected.getDraggableModel());
        } else {
            this.onSelectBlock.emit(null);
        }
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
        if (this.selectedDraggable.getDraggableModel().initialDropzone.getDropzoneModel().dropzoneType === DropzoneType.Toolbar) {
            this.selectBlock(null);
        }
        if (this.isDragging) {
            this.stopDragging();
        }
        this.mouseDownStart = {x: -1, y: -1};
    }

    private triggerWorkspaceMouseDown(event: MouseEvent) {
        if (this.selectedDraggable) {
            this.selectedDraggable.select(false);
        }
        this.selectBlock(null);
    }

    private click(event: MouseEvent) {
        if (this.selectedDraggable.getDraggableModel().rootDropzone === DropzoneType.Workspace) {
            this.isConfiguratorOpened = true;
            this.selectBlock(this.selectedDraggable);
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
            if (this.bestDropzone.getDropzoneModel().dropzoneType === DropzoneType.Trash) {
                this.selectedDraggable.select(false);
                this.selectedDraggable = null;
                this.selectBlock(null);
            }
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

    private savePipelineMetaData(metaData: { name: string, description: string }) {
        (this.pipeline.pipelineBlueprint.metadata.labels.find(l => l.name === 'pipeline.name').value as TextValue).value = metaData.name;
        (this.pipeline.pipelineBlueprint.metadata.labels.find(l => l.name === 'pipeline.description').value as TextValue).value = metaData.description;
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
        if (draggable.getDraggableModel().isSelected) {
            this.selectedDraggable = draggable;
            this.selectBlock(this.selectedDraggable);
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
        this.renderer.listen(this.workspaceDropzone.getDropzoneElement().nativeElement, 'click', (evt) => {
            if (this.isDraggableMouseDown) {
                this.isDraggableMouseDown = false;
            } else {
                this.triggerWorkspaceMouseDown(evt);
            }
        });

        this.dropzones.add(this.workspaceDropzone);
        this.dropzones.add(this.dropzoneFactory.createTrashDropzone(this.workspaceContainer, this));

        this.saveTrigger$.pipe(takeUntil(this.isAlive$)).subscribe(() => {
                this.pipeline = this.pipelineConfigurator.updatePipelineModel(this.draggables, this.pipeline);
                this.onUpdatePipeline.emit(this.pipeline);
            }
        );

        this.runTrigger$.pipe(takeUntil(this.isAlive$)).subscribe(() => {
            this.pipeline = this.pipelineConfigurator.updatePipelineModel(this.draggables, this.pipeline);
            this.onRunPipeline.emit(this.pipeline);
        });

        this.inspectTrigger$.pipe(takeUntil(this.isAlive$)).subscribe(() => {
            //TODO: Disable when no pipeline
            this.isInspecting = !this.isInspecting;
        });

        this.buildEditPipeline();
        let pipelineName = (this.pipeline.pipelineBlueprint.metadata.labels.find(l => l.name === 'pipeline.name').value as TextValue).value;
        let pipelineDescription = (this.pipeline.pipelineBlueprint.metadata.labels.find(l => l.name === 'pipeline.description').value as TextValue).value;
        this.pipelineMetaData = {name: pipelineName, description: pipelineDescription};

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
                    parameterSet: conf.parameterSet
                },
                blueprintRef: nextBlueprint.ref,
                initialDropzone: this.workspaceDropzone,
                next: null,
                previous: null,
                rootDropzone: DropzoneType.Workspace,
                isMirror: false,
                isSelected: false,
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

    ngAfterViewInit() {
        this.cd.detectChanges();
    }
}


