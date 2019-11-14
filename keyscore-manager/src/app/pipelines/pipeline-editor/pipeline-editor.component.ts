import {Location} from "@angular/common";
import {Component, OnDestroy, OnInit} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {isSpinnerShowing} from "../../common/loading/loading.reducer";
import {
    LoadFilterDescriptorsAction,
    ResetPipelineAction,
    StopPipelineAction,
    UpdatePipelineAction
} from "../actions/pipelines.actions";
import {share, takeUntil} from "rxjs/internal/operators";
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
} from "../index";
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
                        (onDelete)="stopPipeline(storeEditingPipeline)"
                        (onInspect)="inspectToggle($event)"
            ></header-bar>

            <pipely-workspace [runTrigger$]="runPipeline$"
                              [saveTrigger$]="savePipeline$"
                              [inspectTrigger$]="runInspect$"
                              [agents]="agents$ | async"
                              [pipeline]="(pipeline$ | async)"
                              [blockDescriptors]="blockDescriptorSource$|async"
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
    public pipeline$: Observable<EditingPipelineModel>;
    public filterDescriptors$: Observable<FilterDescriptor[]>;
    public isLoading$: Observable<boolean>;

    private alive: Subject<void> = new Subject();

    public savePipelineSource$: Subject<void> = new Subject<void>();
    public savePipeline$: Observable<void> = this.savePipelineSource$.asObservable();

    public runPipelineSource$: Subject<void> = new Subject<void>();
    public runPipeline$: Observable<void> = this.runPipelineSource$.asObservable();

    public runInspectSource$: Subject<boolean> = new Subject<boolean>();
    public runInspect$: Observable<boolean> = this.runInspectSource$.asObservable();

    public blockDescriptorSource$: BehaviorSubject<BlockDescriptor[]> = new BehaviorSubject<BlockDescriptor[]>([]);

    public storeEditingPipeline: EditingPipelineModel;

    public errorState$: Observable<boolean>;
    public errorStatus$: Observable<string>;
    public errorMessage$: Observable<string>;

    public isLoadingDatasetsAfter$: Observable<boolean>;
    public loadingDatasetsErrorAfter$: Observable<boolean>;
    public isLoadingDatasetsBefore$: Observable<boolean>;
    public loadingDatasetsErrorBefore$: Observable<boolean>;

    public applicationConf$:Observable<AppConfig>;

    private showBigLoadingViewOnLoading = true;
    private selectedBlockId: string;
    private amount: number = 10;
    private previewMode: boolean = false;
    private outputDatasets$: Observable<Map<string, Dataset[]>>;
    private inputDatasets$: Observable<Map<string, Dataset[]>>;
    private agents$: Observable<Agent[]>;

    constructor(private store: Store<any>, private location: Location, private pipelyAdapter: PipelyKeyscoreAdapter) {
        this.outputDatasets$ = this.store.pipe(select(getOutputDatasetMap));
        this.inputDatasets$ = this.store.pipe(select(getInputDatasetMap));
    }

    inspectToggle(flag: boolean) {
        console.log("INSPECT:", flag);
        if (flag) {
            this.previewMode = true;
            this.runInspectSource$.next(true);
            this.store.dispatch(new DataPreviewToggleView(true));

        } else {
            this.runInspectSource$.next(false);
            this.previewMode = false;
            this.store.dispatch(new DataPreviewToggleView(false));
        }
    }

    ngOnInit() {
        this.showBigLoadingViewOnLoading = true;

        this.store.dispatch(new LoadFilterDescriptorsAction());
        this.store.dispatch(new LoadAgentsAction());

        this.filterDescriptors$ = this.store.pipe(select(getFilterDescriptors), takeUntil(this.alive));
        this.isLoading$ = this.store.pipe(select(isSpinnerShowing), share());
        this.pipeline$ = this.store.pipe(select(getEditingPipeline), takeUntil(this.alive));
        this.agents$ = this.store.pipe(select(getAgents), takeUntil(this.alive));

        this.applicationConf$ = this.store.pipe(select(selectAppConfig));

        this.pipeline$.subscribe(pipe => {
            this.storeEditingPipeline = pipe;
        });

        this.filterDescriptors$.subscribe(descriptors => {
            this.blockDescriptorSource$.next(descriptors.map(descriptor =>
                this.pipelyAdapter.resolvedParameterDescriptorToBlockDescriptor(descriptor)))
        });

        this.errorState$ = this.store.pipe(select(isError));
        this.errorStatus$ = this.store.pipe(select(selectHttpErrorCode));
        this.errorMessage$ = this.store.pipe(select(selectErrorMessage));

        this.isLoadingDatasetsAfter$ = this.store.pipe(select(getIsLoadingDatasetsAfter));
        this.loadingDatasetsErrorAfter$ = this.store.pipe(select(getLoadingErrorAfter));
        this.isLoadingDatasetsBefore$ = this.store.pipe(select(getIsLoadingDatasetsBefore));
        this.loadingDatasetsErrorBefore$ = this.store.pipe(select(getLoadingErrorBefore));
    }

    public triggerDataSourceCreation() {
        this.store.dispatch(new ExtractFromSelectedBlock(this.selectedBlockId, "before", this.amount));
        this.store.dispatch(new ExtractFromSelectedBlock(this.selectedBlockId, "after", this.amount));
    }

    public ngOnDestroy() {
        this.alive.next();
        this.alive.complete();
    }

    public stopPipeline(pipeline: EditingPipelineModel) {
        console.log("Trying to stop pipeline with id" + JSON.stringify(pipeline.pipelineBlueprint.ref.uuid));
        this.store.dispatch(new StopPipelineAction(pipeline.pipelineBlueprint.ref.uuid));
        this.showBigLoadingViewOnLoading = false;

    }

    public updatePipeline(pipeline: EditingPipelineModel) {
        this.store.dispatch(new UpdatePipelineAction(pipeline));
    }

    public runPipeline(pipeline: EditingPipelineModel) {
        this.store.dispatch(new UpdatePipelineAction(pipeline, true));
        this.showBigLoadingViewOnLoading = false;
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

}
