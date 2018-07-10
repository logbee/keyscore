import {Location} from "@angular/common";
import {Component} from "@angular/core";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {ModalService} from "../../services/modal.service";
import {
    FilterConfiguration,
    FilterDescriptor,
    getEditingPipeline,
    getEditingPipelineIsLocked,
    getFilterCategories,
    getFilterDescriptors,
    InternalPipelineConfiguration,
    PipelineConfiguration,
} from "../pipelines.model";
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
import {share} from "rxjs/internal/operators";
import {isMenuExpanded} from "../../common/sidemenu/sidemenu.reducer";

@Component({
    selector: "pipeline-editor",
    styles: [".filter-component{transition: 0.25s ease-in-out;}"],
    template: `
        <loading-full-view *ngIf="isLoading$|async; else editor"></loading-full-view>

        <ng-template #editor>
            <div class="row justify-content-center ml-2 mt-2">
                <div *ngIf="!blocklyFlag" class="col-3">
                    <pipeline-details [pipeline]="pipeline$ | async"
                                      [locked$]="isLocked$"
                                      (update)="updatePipeline($event)"
                                      (reset)="resetPipeline($event)"
                                      (delete)="deletePipeline($event)"
                                      (lock)="setLocked(true, $event)"
                                      (unlock)="setLocked(false, $event)">
                    </pipeline-details>
                </div>

                <div *ngIf="!blocklyFlag" class="col-9">
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
                <blockly-workspace *ngIf="blocklyFlag" class="col-12"
                                   [filterDescriptors$]="filterDescriptors$"
                                   [categories$]="categories$"
                                   [pipeline]="(pipeline$ | async)"
                                   [isLoading$]="isLoading$"
                                   [isMenuExpanded$]="isMenuExpanded$"
                                   (update)="updatePipelineWithBlockly($event)"></blockly-workspace>
            </div>
        </ng-template>
    `,
})
export class PipelineEditorComponent {
    public pipeline$: Observable<InternalPipelineConfiguration>;
    public isLocked$: Observable<boolean>;
    public filterDescriptors$: Observable<FilterDescriptor[]>;
    public categories$: Observable<string[]>;
    public blocklyFlag: boolean;
    public isLoading$: Observable<boolean>;
    public isMenuExpanded$: Observable<boolean>;

    constructor(private store: Store<any>, private location: Location, private modalService: ModalService) {
        this.store.dispatch(new LoadFilterDescriptorsAction());

        const config = this.store.select(selectAppConfig);
        config.subscribe((conf) => this.blocklyFlag = conf.getBoolean("keyscore.manager.features.blockly"));

        this.filterDescriptors$ = this.store.select(getFilterDescriptors);
        this.categories$ = this.store.select(getFilterCategories);
        this.isLoading$ = this.store.select(isSpinnerShowing).pipe(share());
        this.isMenuExpanded$ = this.store.select(isMenuExpanded);
        this.isLocked$ = this.store.select(getEditingPipelineIsLocked);
        this.pipeline$ = this.store.select(getEditingPipeline);
        this.pipeline$.subscribe((pipeline) => {
            if (pipeline) {
                this.store.dispatch(new LockEditingPipelineAction(pipeline.filters && pipeline.filters.length > 0));
            }
        });
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
        // this.isLocked = locked;
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
