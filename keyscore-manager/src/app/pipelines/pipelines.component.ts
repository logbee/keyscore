import {Component, OnDestroy} from "@angular/core";
import {Router} from "@angular/router";
import {select, Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs";
import {v4 as uuid} from "uuid";
import {UpdateRefreshTimeAction} from "../common/loading/loading.actions";
import {isSpinnerShowing, selectRefreshTime} from "../common/loading/loading.reducer";
import * as RouterActions from "../router/router.actions";
import {CreatePipelineAction, LoadAllPipelinesAction, UpdatePipelinePollingAction} from "./pipelines.actions";
import {PipelineInstance} from "../models/pipeline-model/PipelineInstance";
import {getPipelineList, PipelinesState} from "./pipelines.reducer";

@Component({
    selector: "keyscore-pipelines",
    template: `
        <header-bar
                [showManualReload]="false"
                [title]="title"
                (onManualRelad)="reload()"> 
        </header-bar>
        <div class=" ml-2 mt-2">
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
                               routerLink="/pipelines/{{pipeline.id}}">
                                <h5>{{pipeline.name}}</h5>
                                <span><health-light [health]="pipeline.health"></health-light></span>
                            </a>
                            <div class="card-body">
                                <small>{{pipeline.description}}</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})
export class PipelinesComponent implements OnDestroy {
    public pipelines$: Observable<PipelineInstance[]>;
    public isLoading$: Observable<boolean>;
    public refreshTime$: Observable<number>;
    public title: string = "Pipelines";

    constructor(private store: Store<PipelinesState>, private router: Router, private translate: TranslateService) {
        this.pipelines$ = this.store.pipe(select(getPipelineList));
        this.isLoading$ = this.store.pipe(select(isSpinnerShowing));
        this.refreshTime$ = this.store.pipe(select(selectRefreshTime));
        this.store.dispatch(new UpdatePipelinePollingAction(true));
        this.store.dispatch(new LoadAllPipelinesAction());
    }

    public ngOnDestroy() {
        this.store.dispatch(new UpdatePipelinePollingAction(false));
    }

    public createPipeline(activeRouting: boolean = false) {
        const pipelineId = uuid();
        this.store.dispatch(new CreatePipelineAction(pipelineId, "New Pipeline", ""));
        if (activeRouting) {
            this.store.dispatch(new RouterActions.Go({
                path: ["pipelines/" + pipelineId, {}],
                query: {},
                extras: {}
            }));
        }
    }

    public updateRefreshTime(refreshTimes: { newRefreshTime: number, oldRefreshTime: number }) {
        this.store.dispatch(new UpdateRefreshTimeAction(refreshTimes.newRefreshTime, refreshTimes.oldRefreshTime));
    }
}
