import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {Observable} from "rxjs";
import {Store} from "@ngrx/store";
import {ModalService} from "../../services/modal.service";
import {FilterChooser} from "./filter-chooser/filter-chooser.component";
import {
    FilterDescriptor,
    FilterModel,
    getEditingPipeline,
    getEditingPipelineIsLocked,
    getFilterCategories,
    getFilterDescriptors,
    PipelineModel
} from "../pipelines.model";

import {
    DeletePipelineAction,
    LoadFilterDescriptorsAction,
    LockEditingPipelineAction,
    MoveFilterAction,
    RemoveFilterAction,
    ResetPipelineAction,
    UpdateFilterAction,
    UpdatePipelineAction
} from "../pipelines.actions";
import {selectAppConfig} from "../../app.config";
import {Go} from "../../router/router.actions";
import {LoadFilterDescriptorAction} from "../filters/filters.actions";

@Component({
    selector: 'pipeline-editor',
    template: `
        <div class="row justify-content-center">
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
                        <span class="font-weight-bold">{{'PIPELINEEDITORCOMPONENT.PIPELINEBLUEPRINT' | translate}}</span>
                        <div *ngIf="!(isLocked$ | async)">
                            <button class="btn btn-success" (click)="addFilter(null)">{{'PIPELINEEDITORCOMPONENT.ADDFILTER' | translate}}</button>
                        </div>
                    </div>
                    <div class="card-body">
                        <pipeline-filter class="filter-component" *ngFor="let filter of (pipeline$ | async).filters; index as i"
                                       [filter]="filter"
                                       [index]="i"
                                       [filterCount]="(pipeline$|async).filters.length"
                                       [parameters]="filter.parameters"
                                       [isEditingPipelineLocked$]="isLocked$"
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
                               [pipeline]="(pipeline$ | async)"></blockly-workspace>
        </div>
    `,
    styles:['.filter-component{transition: 0.25s ease-in-out;}'],
    providers: []
})
export class PipelineEditorComponent implements OnInit {
    pipeline$: Observable<PipelineModel>;
    isLocked$: Observable<boolean>;
    filterDescriptors$: Observable<FilterDescriptor[]>;
    categories$: Observable<string[]>;
    blocklyFlag:boolean;

    constructor(private store: Store<any>, private location: Location, private modalService: ModalService) {
        let config = this.store.select(selectAppConfig);
        this.filterDescriptors$ = this.store.select(getFilterDescriptors);
        this.categories$ = this.store.select(getFilterCategories);

        this.store.dispatch(new LoadFilterDescriptorsAction());

        config.subscribe(conf => this.blocklyFlag = conf.getBoolean('keyscore.manager.blockly'));
    }

    ngOnInit(): void {
        this.isLocked$ = this.store.select(getEditingPipelineIsLocked);
        this.pipeline$ = this.store.select(getEditingPipeline);
        this.pipeline$.subscribe(pipeline => {
            this.store.dispatch(new LockEditingPipelineAction(pipeline.filters && pipeline.filters.length > 0))
        })
    }

    addFilter(pipeline: PipelineModel) {
        this.modalService.show(FilterChooser);
    }

    deletePipeline(pipeline: PipelineModel) {
        this.store.dispatch(new DeletePipelineAction(pipeline.id));
        this.location.back();
    }

    updatePipeline(pipeline: PipelineModel) {
        this.store.dispatch(new UpdatePipelineAction({
            id: pipeline.id,
            name: pipeline.name,
            description: pipeline.description,
            filters: pipeline.filters
        }));
    }

    resetPipeline(pipeline: PipelineModel) {
        this.store.dispatch(new ResetPipelineAction(pipeline.id))
    }

    setLocked(locked: boolean, pipeline: PipelineModel) {
        //this.isLocked = locked;
        this.store.dispatch(new LockEditingPipelineAction(locked))
    }

    moveFilter(filter: { id: string, position: number }) {
        this.store.dispatch(new MoveFilterAction(filter.id, filter.position))
    }


    updateFilter(update: { filterModel: FilterModel, values: any }) {
        console.log(JSON.stringify(update));
        this.store.dispatch(new UpdateFilterAction(update.filterModel, update.values))
    }

    removeFilter(filter: FilterModel) {
        this.store.dispatch(new RemoveFilterAction(filter.id))
    }

    callLiveEditing(filter: FilterModel) {
        this.store.dispatch(new Go({path: ['pipelines/filter/' + filter.id + '/']}))
    }
}
