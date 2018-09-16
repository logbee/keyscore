import {Location} from "@angular/common";
import {Component, OnDestroy} from "@angular/core";
import {Store} from "@ngrx/store";
import {Observable, Subject} from "rxjs";
import {ModalService} from "../../services/modal.service";
import {isSpinnerShowing} from "../../common/loading/loading.reducer";
import {Go} from "../../router/router.actions";
import {
    DeletePipelineAction,
    LoadFilterDescriptorsAction,
    LockEditingPipelineAction,
    MoveFilterAction,
    RemoveFilterAction,
    ResetPipelineAction,
    UpdateFilterAction,
    UpdatePipelineAction,
    UpdatePipelineWithBlocklyAction,
} from "../pipelines.actions";
import {map, share, takeUntil} from "rxjs/internal/operators";
import {isMenuExpanded} from "../../common/sidemenu/sidemenu.reducer";
import {InternalPipelineConfiguration} from "../../models/pipeline-model/InternalPipelineConfiguration";
import {FilterDescriptor, ResolvedFilterDescriptor} from "../../models/filter-model/FilterDescriptor";
import {
    getEditingPipeline,
    getEditingPipelineIsLocked,
    getFilterCategories,
    getFilterDescriptors,
    getLastUpdateSuccess
} from "../pipelines.reducer";
import {PipelineConfiguration} from "../../models/pipeline-model/PipelineConfiguration";
import {FilterConfiguration} from "../../models/filter-model/FilterConfiguration";

@Component({
    selector: "pipeline-editor",
    styles: [".filter-component{transition: 0.25s ease-in-out;}"],
    template: `
        <header-bar [title]="'Pipeline Editor'"></header-bar>

        <loading-full-view *ngIf="isLoading$|async; else editor"></loading-full-view>

        <ng-template #editor>
            
            <pipely-workspace [pipeline]="(pipeline$ | async)" fxFill=""></pipely-workspace>

            <alert [level]="'success'" [message]="'BLOCKLY.SAVE_SUCCESS'"
                   [trigger$]="successAlertTrigger$"></alert>
            <alert [level]="'danger'" [message]="'BLOCKLY.SAVE_FAILURE'"
                   [trigger$]="failureAlertTrigger$"></alert>
        </ng-template>
    `,
})
export class PipelineEditorComponent implements OnDestroy {
    public pipeline$: Observable<InternalPipelineConfiguration>;
    public isLocked$: Observable<boolean>;
    public filterDescriptors$: Observable<ResolvedFilterDescriptor[]>;
    public categories$: Observable<string[]>;
    public isLoading$: Observable<boolean>;
    public isMenuExpanded$: Observable<boolean>;
    public updateSuccess$: Observable<boolean[]>;

    public successAlertTrigger$: Observable<boolean>;
    public failureAlertTrigger$: Observable<boolean>;

    private alive: Subject<void> = new Subject();

    constructor(private store: Store<any>, private location: Location, private modalService: ModalService) {
        this.store.dispatch(new LoadFilterDescriptorsAction());

        this.filterDescriptors$ = this.store.select(getFilterDescriptors);
        this.categories$ = this.store.select(getFilterCategories);
        this.isLoading$ = this.store.select(isSpinnerShowing).pipe(share());
        this.updateSuccess$ = this.store.select(getLastUpdateSuccess).pipe(share());
        this.isMenuExpanded$ = this.store.select(isMenuExpanded);
        this.isLocked$ = this.store.select(getEditingPipelineIsLocked);
        this.pipeline$ = this.store.select(getEditingPipeline);
        this.pipeline$.pipe(takeUntil(this.alive)).subscribe((pipeline) => {
            if (pipeline) {
                this.store.dispatch(new LockEditingPipelineAction(pipeline.filters && pipeline.filters.length > 0));
            }
        });

        this.successAlertTrigger$ = this.updateSuccess$.pipe(map((success) => success[0]));
        this.failureAlertTrigger$ = this.updateSuccess$.pipe(
            map((successList) => !successList[0]));

    }

    public ngOnDestroy() {
        this.alive.next();
    }

    public deletePipeline(pipeline: InternalPipelineConfiguration) {
        this.store.dispatch(new DeletePipelineAction(pipeline.id));
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

    public updatePipelineWithBlockly(pipelineConfiguration: PipelineConfiguration) {
        this.store.dispatch(new UpdatePipelineWithBlocklyAction(pipelineConfiguration));
    }

    public resetPipeline(pipeline: InternalPipelineConfiguration) {
        this.store.dispatch(new ResetPipelineAction(pipeline.id));
    }

    public setLocked(locked: boolean, pipeline: InternalPipelineConfiguration) {
        this.store.dispatch(new LockEditingPipelineAction(locked));
    }

    public moveFilter(filter: { id: string, position: number }) {
        this.store.dispatch(new MoveFilterAction(filter.id, filter.position));
    }

    public updateFilter(update: { filterConfiguration: FilterConfiguration, values: any }) {
        this.store.dispatch(new UpdateFilterAction(update.filterConfiguration, update.values));
    }

    public removeFilter(filter: FilterConfiguration) {
        this.store.dispatch(new RemoveFilterAction(filter.id));
    }

    public callLiveEditing(filter: FilterConfiguration) {
        this.store.dispatch(new Go({path: ["pipelines/filter/" + filter.id + "/"]}));
    }
}
