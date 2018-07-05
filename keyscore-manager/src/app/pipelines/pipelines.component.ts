import {Component, OnDestroy} from '@angular/core';
import {select, Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {v4 as uuid} from 'uuid'
import {getPipelineList, PipelineInstance, PipelinesState} from "./pipelines.model";
import {CreatePipelineAction, LoadAllPipelinesAction, UpdatePipelinePollingAction} from "./pipelines.actions";
import {Router} from "@angular/router";
import * as RouterActions from '../router/router.actions';
import {TranslateService} from "@ngx-translate/core";
import {isSpinnerShowing, selectRefreshTime} from "../loading/loading.reducer";
import {UpdateRefreshTimeAction} from "../loading/loading.actions";

@Component({
    selector: 'keyscore-pipelines',
    template: `
        <div class="card">
            <div class="card-header">

                <div class="row">
                    <div class="col-1 mr-5">
                        <button type="button" class="btn btn-success" (click)="createPipeline(true)">
                            {{'PIPELINECOMPONENT.CREATE' | translate}}
                        </button>
                    </div>

                    <div class="col-1">
                        <refresh-time [refreshTime]="refreshTime$|async"
                                      (update)="updateRefreshTime($event)"></refresh-time>
                    </div>
                    <loading *ngIf="isLoading$|async" class="col-1 ml-auto align-self-center"></loading>
                </div>
            </div>
            <div class="input-group card-body mt-1">
                <input type="text" class="form-control" placeholder="search..." aria-label="search">
                <div class="input-group-append">
                    <img class="input-group-text" width="40em" height="40em"
                         src="/assets/images/magnifying-glass.svg"/>
                </div>
            </div>
        </div>
        <div class="row mt-3">
            <div class="col-12">
                <div class="card-columns">
                    <div *ngFor="let pipeline of pipelines$ | async; let i = index" class="card">
                        <a class="card-header btn d-flex justify-content-between"
                           routerLink="/pipelines/pipeline/{{pipeline.id}}">
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
    isLoading$: Observable<boolean>;
    refreshTime$: Observable<number>;

    constructor(private store: Store<PipelinesState>, private router: Router, private translate: TranslateService) {
        this.pipelines$ = this.store.pipe(select(getPipelineList));
        this.isLoading$ = this.store.pipe(select(isSpinnerShowing));
        this.refreshTime$ = this.store.pipe(select(selectRefreshTime));
        this.store.dispatch(new UpdatePipelinePollingAction(true));
        this.store.dispatch(new LoadAllPipelinesAction());
    }

    ngOnDestroy() {
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

    updateRefreshTime(refreshTimes:{newRefreshTime:number,oldRefreshTime:number}) {
        this.store.dispatch(new UpdateRefreshTimeAction(refreshTimes.newRefreshTime,refreshTimes.oldRefreshTime));
    }
}
