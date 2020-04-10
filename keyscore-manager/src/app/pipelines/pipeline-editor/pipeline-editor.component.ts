import {Location} from "@angular/common";
import {Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {combineLatest, Observable, Subject} from "rxjs";
import {isSpinnerShowing} from "../../common/loading/loading.reducer";
import {
    LoadFilterDescriptorsAction,
    ResetPipelineAction,
    StopPipelineAction,
    UpdatePipelineAction
} from "../actions/pipelines.actions";
import {map, share, takeUntil, withLatestFrom} from "rxjs/internal/operators";
import {PipelyKeyscoreAdapter} from "../../services/pipely-keyscore-adapter.service";
import {BlockDescriptor} from "./pipely/models/block-descriptor.model";
import {isError, selectErrorMessage, selectHttpErrorCode} from "../../common/error/error.reducer";
import {
    getEditingPipeline,
    getFilterDescriptors,
    getInputDatasetMap,
    getIsLoadingDatasetsAfter,
    getIsLoadingDatasetsBefore,
    getLoadingErrorAfter,
    getLoadingErrorBefore,
    getOutputDatasetMap
} from "../reducers/module";
import {DataPreviewToggleView, ExtractFromSelectedBlock} from "../actions/preview.actions";
import {DraggableModel} from "./pipely/models/draggable.model";
import {EditingPipelineModel} from "@/../modules/keyscore-manager-models/src/main/pipeline-model/EditingPipelineModel";
import {FilterDescriptor} from "@/../modules/keyscore-manager-models/src/main/descriptors/FilterDescriptor";
import {InternalPipelineConfiguration} from "@/../modules/keyscore-manager-models/src/main/pipeline-model/InternalPipelineConfiguration";
import {Dataset} from "@/../modules/keyscore-manager-models/src/main/dataset/Dataset";
import {LoadAgentsAction} from "@/app/agents/agents.actions";
import {Agent} from "@keyscore-manager-models/src/main/common/Agent";
import {getAgents} from "@/app/agents/agents.reducer";
import {AppConfig, selectAppConfig} from "@/app/app.config";
import {Title} from "@angular/platform-browser";
import {PipelineConfigurationChecker} from "@/app/pipelines/services/pipeline-configuration-checker.service";
import {setTabTitleFromPipeline} from "@/app/pipelines/pipeline-editor/rxjs-operators/set-tab-title-from-pipeline.operator";
import {filterDescriptorsNotInPipeline} from "@/app/pipelines/pipeline-editor/rxjs-operators/filter-descriptors-not-in-pipeline.operator";
import {alignPipelineConfigWithDescriptors} from "@/app/pipelines/pipeline-editor/rxjs-operators/align-pipeline-config-with-descriptors.operator";
import {HeaderBarComponent} from "@/app/common/headerbar/headerbar.component";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";
import {SnackbarOpen} from "@/app/common/snackbar/snackbar.actions";

@Component({
    selector: "pipeline-editor",
    template: `
        <loading-full-view *ngIf="((isLoading$|async) && showBigLoadingViewOnLoading); else error"></loading-full-view>

        <ng-template #error>
            <error-component *ngIf="(errorState$ | async);else fullComponent"
                             [httpError]="(errorStatus$ | async)"
                             [message]="(errorMessage$ | async)">
            </error-component>
        </ng-template>

        <ng-template #fullComponent>
            <header-bar [title]="'Pipeline Editor'" [showSave]="true" [showRun]="true" [showDelete]="true"
                        [showInspect]="true"
                        [isLoading]="isLoading$|async"
                        (onSave)="savePipelineSource$.next()"
                        (onRun)="runPipelineSource$.next()"
                        (onInspect)="inspectToggle($event)"
            ></header-bar>

            <pipely-workspace [runTrigger$]="runPipeline$"
                              [saveTrigger$]="savePipeline$"
                              [inspectTrigger$]="runInspect$"
                              [agents]="agents$ | async"
                              [pipeline]="editingPipeline$ | async"
                              [blockDescriptors]="blockDescriptors$|async"
                              [inputDatasets]="inputDatasets$|async"
                              [outputDatasets]="outputDatasets$|async"
                              [isLoadingDatasetsAfter]="isLoadingDatasetsAfter$|async"
                              [isLoadingDatasetsBefore]="isLoadingDatasetsBefore$|async"
                              [loadingDatasetsErrorAfter]="loadingDatasetsErrorAfter$|async"
                              [loadingDatasetsErrorBefore]="loadingDatasetsErrorBefore$|async"
                              [applicationConf]="applicationConf$|async"
                              (onUpdatePipeline)="updatePipeline($event)"
                              (onRunPipeline)="runPipeline($event)"
                              (onSelectBlock)="selectBlock($event)"
                              fxFill>
            </pipely-workspace>
        </ng-template>
    `,
})
export class PipelineEditorComponent implements OnInit, OnDestroy {
    readonly NUMBER_OF_DATASETS_TO_EXTRACT: number = 10;

    pipeline$: Observable<EditingPipelineModel>;
    filterDescriptors$: Observable<FilterDescriptor[]>;
    isLoading$: Observable<boolean>;

    savePipelineSource$: Subject<void> = new Subject<void>();
    savePipeline$: Observable<void> = this.savePipelineSource$.asObservable();

    runPipelineSource$: Subject<void> = new Subject<void>();
    runPipeline$: Observable<void> = this.runPipelineSource$.asObservable();

    runInspectSource$: Subject<boolean> = new Subject<boolean>();
    runInspect$: Observable<boolean> = this.runInspectSource$.asObservable();

    blockDescriptors$: Observable<BlockDescriptor[]>;

    errorState$: Observable<boolean>;
    errorStatus$: Observable<string>;
    errorMessage$: Observable<string>;

    isLoadingDatasetsAfter$: Observable<boolean>;
    loadingDatasetsErrorAfter$: Observable<boolean>;
    isLoadingDatasetsBefore$: Observable<boolean>;
    loadingDatasetsErrorBefore$: Observable<boolean>;

    applicationConf$: Observable<AppConfig>;
    showBigLoadingViewOnLoading = true;

    private unsubscribe$: Subject<void> = new Subject();
    private selectedBlockId: string;
    private outputDatasets$: Observable<Map<string, Dataset[]>>;
    private inputDatasets$: Observable<Map<string, Dataset[]>>;
    private agents$: Observable<Agent[]>;

    private editingPipeline$: Observable<EditingPipelineModel>;
    private parametersWhichWereAlignedToMatchDescriptors: Map<string, ParameterRef[]> = new Map();

    @ViewChild(HeaderBarComponent, { read: HeaderBarComponent }) set headerBar(headerBarComponent: HeaderBarComponent) {
        if (headerBarComponent) {
            headerBarComponent.onDelete.pipe(
                withLatestFrom(this.editingPipeline$),
                takeUntil(this.unsubscribe$)).subscribe(([_, pipeline]) =>
                this.stopPipeline(pipeline)
            );

        }
    };

    constructor(
        private store: Store<any>,
        private location: Location,
        private pipelyAdapter: PipelyKeyscoreAdapter,
        private titleService: Title,
        private pipelineConfigurationChecker: PipelineConfigurationChecker) {

        this.outputDatasets$ = this.store.pipe(select(getOutputDatasetMap));
        this.inputDatasets$ = this.store.pipe(select(getInputDatasetMap));
    }

    inspectToggle(isPreview: boolean) {
        this.runInspectSource$.next(isPreview);
        this.store.dispatch(new DataPreviewToggleView(isPreview));
    }

    ngOnInit() {
        this.showBigLoadingViewOnLoading = true;

        this.store.dispatch(new LoadFilterDescriptorsAction());
        this.store.dispatch(new LoadAgentsAction());

        this.filterDescriptors$ = this.store.pipe(select(getFilterDescriptors));
        this.pipeline$ = this.store.pipe(select(getEditingPipeline));

        this.isLoading$ = this.store.pipe(select(isSpinnerShowing), share());

        const pipeline$ = this.pipeline$.pipe(setTabTitleFromPipeline(this.titleService));

        const pipelineAndDescriptor$ = combineLatest(pipeline$, this.filterDescriptors$).pipe(filterDescriptorsNotInPipeline());
        this.editingPipeline$ = pipelineAndDescriptor$.pipe(
            alignPipelineConfigWithDescriptors(this.pipelineConfigurationChecker));

        this.pipelineConfigurationChecker.changedParameters$.pipe(takeUntil(this.unsubscribe$)).subscribe(changedParameters => {
            this.parametersWhichWereAlignedToMatchDescriptors = changedParameters;
        });

        this.agents$ = this.store.pipe(select(getAgents), takeUntil(this.unsubscribe$));

        this.applicationConf$ = this.store.pipe(select(selectAppConfig));

        this.blockDescriptors$ = this.filterDescriptors$.pipe(
            map(descriptors =>
                descriptors.map(this.pipelyAdapter.filterDescriptorToBlockDescriptor)),
            takeUntil(this.unsubscribe$));

        this.errorState$ = this.store.pipe(select(isError));
        this.errorStatus$ = this.store.pipe(select(selectHttpErrorCode));
        this.errorMessage$ = this.store.pipe(select(selectErrorMessage));

        this.isLoadingDatasetsAfter$ = this.store.pipe(select(getIsLoadingDatasetsAfter));
        this.loadingDatasetsErrorAfter$ = this.store.pipe(select(getLoadingErrorAfter));
        this.isLoadingDatasetsBefore$ = this.store.pipe(select(getIsLoadingDatasetsBefore));
        this.loadingDatasetsErrorBefore$ = this.store.pipe(select(getLoadingErrorBefore));
    }


    public triggerDataSourceCreation() {
        this.store.dispatch(new ExtractFromSelectedBlock(this.selectedBlockId, "before", this.NUMBER_OF_DATASETS_TO_EXTRACT));
        this.store.dispatch(new ExtractFromSelectedBlock(this.selectedBlockId, "after", this.NUMBER_OF_DATASETS_TO_EXTRACT));
    }

    public ngOnDestroy() {
        this.titleService.setTitle("KEYSCORE");
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public stopPipeline(pipeline: EditingPipelineModel) {
        console.log("Trying to stop pipeline with id" + JSON.stringify(pipeline.pipelineBlueprint.ref.uuid));
        this.store.dispatch(new StopPipelineAction(pipeline.pipelineBlueprint.ref.uuid));
        this.showBigLoadingViewOnLoading = false;

    }

    public updatePipeline(pipeline: EditingPipelineModel) {
        if (this.allParameterChangesConfirmed()) {
            this.store.dispatch(new UpdatePipelineAction(pipeline));
        } else {
            this.store.dispatch(
                new SnackbarOpen(
                    {
                        message: 'Some filters contain errors. Please fix them before saving the pipeline',
                        action: 'Error',
                        config: {
                            horizontalPosition: "center",
                            verticalPosition: "top",
                            duration: 15000
                        }
                    }
                ))
        }
    }

    public runPipeline(pipeline: EditingPipelineModel) {
        if (this.allParameterChangesConfirmed()) {
            this.store.dispatch(new UpdatePipelineAction(pipeline, true));
            this.showBigLoadingViewOnLoading = false;
        } else {
            this.store.dispatch(
                new SnackbarOpen(
                    {
                        message: 'Some filters contain errors. Please fix them before running the pipeline',
                        action: 'Error',
                        config: {
                            horizontalPosition: "center",
                            verticalPosition: "top",
                            duration: 15000
                        }
                    }
                ))
        }
    }

    public resetPipeline(pipeline: InternalPipelineConfiguration) {
        this.store.dispatch(new ResetPipelineAction(pipeline.id));
    }

    public selectBlock(selectedBlockId: string) {
        this.selectedBlockId = selectedBlockId;
        this.triggerDataSourceCreation()
    }

    public isSink(draggable: DraggableModel) {
        return draggable.blockDescriptor.nextConnection === undefined;
    }

    private allParameterChangesConfirmed(): boolean {
        const parameters = Array.from(this.parametersWhichWereAlignedToMatchDescriptors.values());
        return !parameters.some(filterRefs => filterRefs.length > 0);
    }
}
