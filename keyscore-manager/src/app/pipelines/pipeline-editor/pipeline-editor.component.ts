import {Location} from "@angular/common";
import {Component, OnDestroy} from "@angular/core";
import {Store} from "@ngrx/store";
import {Observable, Subject} from "rxjs";
import {ModalService} from "../../services/modal.service";
import {FilterChooser} from "./filter-chooser/filter-chooser.component";
import {selectAppConfig} from "../../app.config";
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
import {FilterDescriptor} from "../../models/filter-model/FilterDescriptor";
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
            <div [ngSwitch]="editingGUI">
                <div>
                    <ng-container *ngSwitchCase="'default'">
                        <div class="col-3">
                            <pipeline-details [pipeline]="pipeline$ | async"
                                              [locked$]="isLocked$"
                                              (update)="updatePipeline($event)"
                                              (reset)="resetPipeline($event)"
                                              (delete)="deletePipeline($event)"
                                              (lock)="setLocked(true, $event)"
                                              (unlock)="setLocked(false, $event)">
                            </pipeline-details>
                        </div>

                        <div class="col-9">
                            <div class="card">
                                <div class="card-header d-flex justify-content-between">
                        <span class="font-weight-bold">
                            {{'PIPELINEEDITORCOMPONENT.PIPELINEBLUEPRINT' | translate}}
                        </span>
                                    <div *ngIf="!(isLocked$ | async)">
                                        <button class="btn btn-success" (click)="addFilter(null)">
                                            {{'PIPELINEEDITORCOMPONENT.ADDFILTER' | translate}}
                                        </button>
                                    </div>
                                </div>
                                <div class="card-body">
                                    <pipeline-filter class="filter-component"
                                                     *ngFor="let filter of (pipeline$ | async).filters; index as i"
                                                     [filter]="filter"
                                                     [index]="i"
                                                     [filterCount]="(pipeline$|async).filters.length"
                                                     [parameters]="filter.descriptor.parameters"
                                                     [isEditingPipelineLocked$]="isLocked$"
                                                     [editingPipeline]="pipeline$ | async"
                                                     (move)="moveFilter($event)"
                                                     (remove)="removeFilter($event)"
                                                     (update)="updateFilter($event)"
                                                     (liveEdit)="callLiveEditing($event)">
                                    </pipeline-filter>
                                </div>
                            </div>
                        </div>
                    </ng-container>
                    <blockly-workspace *ngSwitchCase="'blockly'"
                                       [filterDescriptors$]="filterDescriptors$"
                                       [categories$]="categories$"
                                       [pipeline]="(pipeline$ | async)"
                                       [isLoading$]="isLoading$"
                                       [isMenuExpanded$]="isMenuExpanded$"
                                       (update)="updatePipelineWithBlockly($event)"></blockly-workspace>
                    <pipely-workspace *ngSwitchCase="'pipely'" [pipeline]="(pipeline$ | async)" fxFill=""></pipely-workspace>
                </div>

                <alert [level]="'success'" [message]="'BLOCKLY.SAVE_SUCCESS'"
                       [trigger$]="successAlertTrigger$"></alert>
                <alert [level]="'danger'" [message]="'BLOCKLY.SAVE_FAILURE'"
                       [trigger$]="failureAlertTrigger$"></alert>
            </div>
        </ng-template>
    `,
})
export class PipelineEditorComponent implements OnDestroy {
    public pipeline$: Observable<InternalPipelineConfiguration>;
    public isLocked$: Observable<boolean>;
    public filterDescriptors$: Observable<FilterDescriptor[]>;
    public categories$: Observable<string[]>;
    public editingGUI: string;
    public isLoading$: Observable<boolean>;
    public isMenuExpanded$: Observable<boolean>;
    public updateSuccess$: Observable<boolean[]>;

    public successAlertTrigger$: Observable<boolean>;
    public failureAlertTrigger$: Observable<boolean>;

    private alive: Subject<void> = new Subject();

    constructor(private store: Store<any>, private location: Location, private modalService: ModalService) {
        this.store.dispatch(new LoadFilterDescriptorsAction());

        const config = this.store.select(selectAppConfig);
        config.subscribe((conf) => this.editingGUI = conf.getString("keyscore.manager.features.editing-gui"));

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

    public addFilter(pipeline: InternalPipelineConfiguration) {
        this.modalService.show(FilterChooser);
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
