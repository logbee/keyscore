import {Component, OnDestroy} from '@angular/core';
import {select, Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {v4 as uuid} from 'uuid'
import {getPipelineList, PipelineInstance, PipelinesState} from "./pipelines.model";
import {CreatePipelineAction, LoadAllPipelinesAction, UpdatePipelinePollingAction} from "./pipelines.actions";
import {Router} from "@angular/router";
import * as RouterActions from '../router/router.actions';
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: 'keyscore-pipelines',
    template: `
        <div class="row">
            <div class="col-3">
                <div class="card">
                    <div class="card-body">
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <img class="input-group-text" width="48em" height="48em"
                                     src="/assets/images/magnifying-glass.svg"/>
                            </div>
                            <input type="text" class="form-control" placeholder="search..." aria-label="search">
                        </div>
                        <div class="mt-3 mb-3">
                            <button type="button" class="btn btn-success" (click)="createPipeline(true)">
                                {{'PIPELINECOMPONENT.CREATE' | translate}}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-9">
                <div class="card-columns">
                    <div *ngFor="let pipeline of pipelines$ | async; let i = index" class="card">
                        <a class="card-header btn d-flex justify-content-between" routerLink="/pipelines/pipeline/{{pipeline.id}}">
                            <h5>{{pipeline.name}}</h5><span class="health-light-{{pipeline.health}}"></span>
                        </a>
                        <div class="card-body">
                            <small>{{pipeline.description}}</small>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})
export class PipelinesComponent implements OnDestroy {
    pipelines$: Observable<PipelineInstance[]>;

    constructor(private store: Store<PipelinesState>, private router: Router, private translate: TranslateService) {
        this.pipelines$ = this.store.pipe(select(getPipelineList));
        this.store.dispatch(new UpdatePipelinePollingAction(true));
        this.store.dispatch(new LoadAllPipelinesAction());
    }

    ngOnDestroy(){
        this.store.dispatch(new UpdatePipelinePollingAction(false));
    }

    createPipeline(activeRouting: boolean = false) {
        let pipelineId = uuid();
        this.store.dispatch(new CreatePipelineAction(pipelineId, "New Pipeline", ""));
        if (activeRouting) {
            this.store.dispatch(new RouterActions.Go({
                path: ['pipelines/pipeline/' + pipelineId, {}],
                query: {},
                extras: {}
            }));
        }
    }
}
