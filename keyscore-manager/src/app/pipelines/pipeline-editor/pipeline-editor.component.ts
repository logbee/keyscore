import {Location} from "@angular/common";
import {Component, OnDestroy, OnInit} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {Observable, Subject} from "rxjs";
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
import {_} from "lodash";
import {Blueprint} from "../../models/blueprints/Blueprint";

@Component({
    selector: "pipeline-editor",
    template: `
        <header-bar [title]="'Pipeline Editor'" [showSave]="true" [showRun]="true" [showDelete]="true"
                    (onSave)="savePipelineSource$.next()"></header-bar>

        <loading-full-view *ngIf="isLoading$|async; else editor"></loading-full-view>

        <ng-template #editor>

            <pipely-workspace [saveTrigger$]="savePipeline$" [pipeline]="(pipeline$ | async)"
                              [blockDescriptors$]="pipelyBlockDescriptors$" (onUpdatePipeline)="updatePipeline($event)"
                              fxFill></pipely-workspace>

        </ng-template>
    `,
})
export class PipelineEditorComponent implements OnInit,OnDestroy {
    public pipeline$: Observable<EditingPipelineModel>;
    public filterDescriptors$: Observable<ResolvedFilterDescriptor[]>;
    public isLoading$: Observable<boolean>;
    public isMenuExpanded$: Observable<boolean>;

    private alive: Subject<void> = new Subject();

    public savePipelineSource$: Subject<void> = new Subject<void>();
    public savePipeline$: Observable<void> = this.savePipelineSource$.asObservable();

    public blockDescriptorSource$: Subject<BlockDescriptor[]> = new Subject();
    public pipelyBlockDescriptors$: Observable<BlockDescriptor[]> = this.blockDescriptorSource$.asObservable();

    public storeEditingPipeline: EditingPipelineModel;

    constructor(private store: Store<any>, private location: Location, private pipelyAdapter: PipelyKeyscoreAdapter) {
    }

    ngOnInit(){
        this.store.dispatch(new LoadFilterDescriptorsAction());

        this.filterDescriptors$ = this.store.pipe(select(getFilterDescriptors),takeUntil(this.alive));
        this.isLoading$ = this.store.pipe(select(isSpinnerShowing),share());
        this.isMenuExpanded$ = this.store.pipe(select(isMenuExpanded));
        this.pipeline$ = this.store.pipe(select(getEditingPipeline,share()));

        this.pipeline$.pipe(takeUntil(this.alive)).subscribe(pipe => this.storeEditingPipeline = pipe);

        this.filterDescriptors$.subscribe(descriptors => {
            this.blockDescriptorSource$.next(descriptors.map(descriptor =>
                this.pipelyAdapter.resolvedParameterDescriptorToBlockDescriptor(descriptor)))
        });
    }

    public ngOnDestroy() {
        this.alive.next();
    }

    public deletePipeline(pipeline: EditingPipelineModel) {
        this.store.dispatch(new DeletePipelineAction(pipeline.pipelineBlueprint.ref.uuid));
        this.location.back();
    }

    public updatePipeline(pipeline: EditingPipelineModel) {
        let pipelineChanges: EditingPipelineModel = {
            pipelineBlueprint: null,
            configurations: [],
            blueprints: []
        };

        //TODO: only send pipelineBlueprint if it has changed, therefore proper pipeline creation has to be implemented first
        /*pipelineChanges.pipelineBlueprint = !_.isEqual(this.storeEditingPipeline.pipelineBlueprint, pipeline.pipelineBlueprint) ?
            pipeline.pipelineBlueprint : null;
        */
        pipelineChanges.pipelineBlueprint = pipeline.pipelineBlueprint;
        pipelineChanges.blueprints = this.blueprintsDifference(this.storeEditingPipeline.blueprints, pipeline.blueprints);
        pipelineChanges.configurations = this.configurationsDifference(this.storeEditingPipeline.configurations, pipeline.configurations);

        this.store.dispatch(new UpdatePipelineAction(pipelineChanges));
    }

    public resetPipeline(pipeline: InternalPipelineConfiguration) {
        this.store.dispatch(new ResetPipelineAction(pipeline.id));
    }

    public callLiveEditing(filter: Configuration) {
        this.store.dispatch(new Go({path: ["pipelines/filter/" + filter.ref.uuid + "/"]}));
    }

    private configurationsDifference(old: Configuration[], updated: Configuration[]): Configuration[] {
        let result: Configuration[] = [];
        updated.forEach(configuration => {
            const oldConfigurationIndex = old.findIndex(oldConfiguration =>
                oldConfiguration.ref.uuid === configuration.ref.uuid);
            if (oldConfigurationIndex === -1 || (oldConfigurationIndex !== -1 && !_.isEqual(configuration, old[oldConfigurationIndex]))) {
                result.push(configuration);
            }
        });

        return result;
    }

    private blueprintsDifference(old: Blueprint[], updated: Blueprint[]): Blueprint[] {
        let result: Blueprint[] = [];
        updated.forEach(blueprint => {
            const oldBlueprintIndex = old.findIndex(oldBlueprint =>
                oldBlueprint.ref.uuid === blueprint.ref.uuid);
            if (oldBlueprintIndex === -1 || (oldBlueprintIndex !== -1 && !_.isEqual(blueprint, old[oldBlueprintIndex]))) {
                result.push(blueprint);
            }
        });

        return result;
    }
}
