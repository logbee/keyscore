import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {Observable, Subject} from "rxjs";
import {v4 as uuid} from "uuid";
import {UpdateRefreshTimeAction} from "../common/loading/loading.actions";
import {isSpinnerShowing, selectRefreshTime} from "../common/loading/loading.reducer";
import * as RouterActions from "../router/router.actions";
import {CreatePipelineAction, LoadPipelineBlueprints, UpdatePipelinePollingAction} from "./actions/pipelines.actions";
import {getPipelineList} from "./index";
import {MatPaginator, MatSort} from "@angular/material";
import {PipelinesState} from "./reducers/pipelines.reducer";
import {DataSourceFactory} from "../data-source/data-source-factory";
import {PipelineDataSource} from "../data-source/pipeline-data-source";
import {Ref} from "@/../modules/keyscore-manager-models/src/main/common/Ref";
import {takeUntil} from "rxjs/operators";

@Component({
    selector: "keyscore-pipelines",
    template: `
        <header-bar
                [showManualReload]="false"
                [title]="title"
                [showRefresh]="refreshTime$|async"
                [showAdd]="true"
                [isLoading]="isLoading$|async"
                (onAdd)="createPipeline(true)"
                (onUpdateRefreshTime)="updateRefreshTime($event)"
                (onManualReload)="reload()">
        </header-bar>

        <div fxLayout="column" fxLayoutGap="15px" class="table-wrapper">
            <!--Search Field-->
            <mat-form-field fxFlex="5" class="search-position">
                <input matInput (keyup)="applyFilter($event.target.value)"
                       placeholder="{{'GENERAL.SEARCH' | translate}}">
                <button mat-button matSuffix mat-icon-button aria-label="Search">
                    <mat-icon>search</mat-icon>
                </button>
            </mat-form-field>

            <pipelines-overview [dataSource]="dataSource" [selectionMode]="false" (deployPipeline)="deployPipeline($event[0], $event[1])" (editPipeline)="editPipeline($event)"></pipelines-overview>
        </div>
    `
})

export class PipelinesComponent implements OnDestroy, OnInit, AfterViewInit {
    public isLoading$: Observable<boolean>;
    public refreshTime$: Observable<number>;
    public title: string = "Pipelines";
    public configs: Observable<Ref[]>;
    dataSource: PipelineDataSource;

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    private _unsubscribe$: Subject<void> = new Subject<void>();

    constructor(private store: Store<PipelinesState>, private dataSourceFactory: DataSourceFactory) {

    }

    public ngOnInit() {
        this.dataSource = new PipelineDataSource([]);
        this.store.pipe(select(getPipelineList)).pipe(takeUntil(this._unsubscribe$)).subscribe(list => {
            this.dataSource.data = list;
        });
        this.isLoading$ = this.store.pipe(select(isSpinnerShowing));
        this.refreshTime$ = this.store.pipe(select(selectRefreshTime));
        this.store.dispatch(new UpdatePipelinePollingAction(true));
        this.store.dispatch(new LoadPipelineBlueprints());
    }

    public ngOnDestroy() {
        this._unsubscribe$.next();
        this._unsubscribe$.complete();
        this.store.dispatch(new UpdatePipelinePollingAction(false));
    }

    public ngAfterViewInit() {

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

    public editPipeline(id: string) {
        //this.store.dispatch(new EditPipelineAction(id));
        this.store.dispatch(new RouterActions.Go({
            path: ["pipelines/" + id, {}],
            query: {},
            extras: {}
        }));
    }

    private deployPipeline(id: string, deploy: boolean): void {
        console.log("Deploy pipleine '" + id + "': " + deploy)
    }

    public updateRefreshTime(refreshTimes: { newRefreshTime: number, oldRefreshTime: number }) {
        this.store.dispatch(new UpdateRefreshTimeAction(refreshTimes.newRefreshTime, refreshTimes.oldRefreshTime));
    }

    applyFilter(filterValue: string) {
        this.dataSource.filter = filterValue;
        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage()
        }
    }
}
