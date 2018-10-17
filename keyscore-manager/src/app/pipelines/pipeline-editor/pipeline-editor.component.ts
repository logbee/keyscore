import {Location} from "@angular/common";
import {Component, OnDestroy, OnInit} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {isSpinnerShowing} from "../../common/loading/loading.reducer";
import {Go} from "../../router/router.actions";
import {
    DeletePipelineAction,
    LoadFilterDescriptorsAction,
    ResetPipelineAction,
    UpdatePipelineAction
} from "../pipelines.actions";
import {share, take, takeUntil} from "rxjs/internal/operators";
import {isMenuExpanded} from "../../common/sidemenu/sidemenu.reducer";
import {InternalPipelineConfiguration} from "../../models/pipeline-model/InternalPipelineConfiguration";
import {ResolvedFilterDescriptor} from "../../models/descriptors/FilterDescriptor";
import {getEditingPipeline, getFilterDescriptors} from "../pipelines.reducer";
import {Configuration} from "../../models/common/Configuration";
import {EditingPipelineModel} from "../../models/pipeline-model/EditingPipelineModel";
import {PipelyKeyscoreAdapter} from "../../services/pipely-keyscore-adapter.service";
import {BlockDescriptor} from "./pipely/models/block-descriptor.model";
import * as _ from "lodash";
import {Blueprint} from "../../models/blueprints/Blueprint";
import {
    errorState,
    ErrorState,
    isError,
    selectErrorMessage,
    selectHttpErrorCode
} from "../../common/error/error.reducer";

@Component({
    selector: "pipeline-editor",
    template: `
        <loading-full-view *ngIf="isLoading$|async; else error"></loading-full-view>

        <ng-template #error>
            <error-component *ngIf="(errorState$ | async);else fullComponent"
                             [httpError]="(errorStatus$ | async)"
                             [message]="(errorMessage$ | async)">
            </error-component>
        </ng-template>

        <ng-template #fullComponent>
            <header-bar [title]="'Pipeline Editor'" [showSave]="true" [showRun]="true" [showDelete]="true"
                        (onSave)="savePipelineSource$.next()"
                        (onRun)="runPipelineSource$.next()"
            ></header-bar>

            <pipely-workspace [runTrigger$]="runPipeline$" [saveTrigger$]="savePipeline$"
                              [pipeline]="(pipeline$ | async)"
                              [blockDescriptors]="blockDescriptorSource$|async"
                              (onUpdatePipeline)="updatePipeline($event)"
                              (onRunPipeline)="runPipeline($event)"
                              fxFill></pipely-workspace>
        </ng-template>


    `,
})
export class PipelineEditorComponent implements OnInit, OnDestroy {
    public pipeline$: Observable<EditingPipelineModel>;
    public filterDescriptors$: Observable<ResolvedFilterDescriptor[]>;
    public isLoading$: Observable<boolean>;

    private alive: Subject<void> = new Subject();

    public savePipelineSource$: Subject<void> = new Subject<void>();
    public savePipeline$: Observable<void> = this.savePipelineSource$.asObservable();

    public runPipelineSource$: Subject<void> = new Subject<void>();
    public runPipeline$: Observable<void> = this.runPipelineSource$.asObservable();

    public blockDescriptorSource$: BehaviorSubject<BlockDescriptor[]> = new BehaviorSubject<BlockDescriptor[]>([]);

    public storeEditingPipeline: EditingPipelineModel;

    public errorState$: Observable<boolean>;
    public errorStatus$: Observable<string>;
    public errorMessage$: Observable<string>;

    constructor(private store: Store<any>, private location: Location, private pipelyAdapter: PipelyKeyscoreAdapter) {
    }

    ngOnInit() {
        this.store.dispatch(new LoadFilterDescriptorsAction());

        this.filterDescriptors$ = this.store.pipe(select(getFilterDescriptors), takeUntil(this.alive));
        this.isLoading$ = this.store.pipe(select(isSpinnerShowing), share());
        this.pipeline$ = this.store.pipe(select(getEditingPipeline), takeUntil(this.alive));

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
    }

    public ngOnDestroy() {
        this.alive.next();
    }

    public deletePipeline(pipeline: EditingPipelineModel) {
        this.store.dispatch(new DeletePipelineAction(pipeline.pipelineBlueprint.ref.uuid));
        this.location.back();
    }

    public updatePipeline(pipeline: EditingPipelineModel) {
        this.store.dispatch(new UpdatePipelineAction(pipeline));
    }

    public runPipeline(pipeline: EditingPipelineModel) {
        this.store.dispatch(new UpdatePipelineAction(pipeline, true));
    }

    public resetPipeline(pipeline: InternalPipelineConfiguration) {
        this.store.dispatch(new ResetPipelineAction(pipeline.id));
    }

    public callLiveEditing(filter: Configuration) {
        this.store.dispatch(new Go({path: ["pipelines/filter/" + filter.ref.uuid + "/"]}));
    }

}
