import {Location} from "@angular/common";
import {Component, OnDestroy} from "@angular/core";
import {Store} from "@ngrx/store";
import {Observable, Subject} from "rxjs";
import {isSpinnerShowing} from "../../common/loading/loading.reducer";
import {Go} from "../../router/router.actions";
import {
    DeletePipelineAction,
    LoadFilterDescriptorsAction,
    ResetPipelineAction,
    UpdatePipelineAction
} from "../pipelines.actions";
import {share, takeUntil} from "rxjs/internal/operators";
import {isMenuExpanded} from "../../common/sidemenu/sidemenu.reducer";
import {InternalPipelineConfiguration} from "../../models/pipeline-model/InternalPipelineConfiguration";
import {ResolvedFilterDescriptor} from "../../models/descriptors/FilterDescriptor";
import {getEditingPipeline, getFilterDescriptors} from "../pipelines.reducer";
import {Configuration} from "../../models/common/Configuration";
import {EditingPipelineModel} from "../../models/pipeline-model/EditingPipelineModel";
import {PipelyKeyscoreAdapter} from "../../services/pipely-keyscore-adapter.service";
import {BlockDescriptor} from "./pipely/models/block-descriptor.model";

@Component({
    selector: "pipeline-editor",
    template: `
        <header-bar [title]="'Pipeline Editor'" [showSave]="true" [showRun]="true" [showDelete]="true"></header-bar>

        <loading-full-view *ngIf="isLoading$|async; else editor"></loading-full-view>

        <ng-template #editor>

            <pipely-workspace [pipeline]="(pipeline$ | async)" [blockDescriptors$]="pipelyBlockDescriptors$"  fxFill=""></pipely-workspace>
            
        </ng-template>
    `,
})
export class PipelineEditorComponent implements OnDestroy {
    public pipeline$: Observable<EditingPipelineModel>;
    public filterDescriptors$: Observable<ResolvedFilterDescriptor[]>;
    public isLoading$: Observable<boolean>;
    public isMenuExpanded$: Observable<boolean>;

    private alive: Subject<void> = new Subject();

    public blockDescriptorSource$:Subject<BlockDescriptor[]> = new Subject();
    public pipelyBlockDescriptors$: Observable<BlockDescriptor[]> = this.blockDescriptorSource$.asObservable();

    constructor(private store: Store<any>, private location: Location, private pipelyAdapter: PipelyKeyscoreAdapter) {
        this.store.dispatch(new LoadFilterDescriptorsAction());

        this.filterDescriptors$ = this.store.select(getFilterDescriptors).pipe(takeUntil(this.alive));
        this.isLoading$ = this.store.select(isSpinnerShowing).pipe(share());
        this.isMenuExpanded$ = this.store.select(isMenuExpanded);
        this.pipeline$ = this.store.select(getEditingPipeline);

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

    public updatePipeline(pipeline: InternalPipelineConfiguration) {
        this.store.dispatch(new UpdatePipelineAction({
            id: pipeline.id,
            name: pipeline.name,
            description: pipeline.description,
            filters: pipeline.filters,
            isRunning: pipeline.isRunning
        }));
    }

    public resetPipeline(pipeline: InternalPipelineConfiguration) {
        this.store.dispatch(new ResetPipelineAction(pipeline.id));
    }

    public callLiveEditing(filter: Configuration) {
        this.store.dispatch(new Go({path: ["pipelines/filter/" + filter.ref.uuid + "/"]}));
    }
}
