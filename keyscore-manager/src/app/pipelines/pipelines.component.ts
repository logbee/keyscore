import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {v4 as uuid} from "uuid";
import {UpdateRefreshTimeAction} from "../common/loading/loading.actions";
import {isSpinnerShowing, selectRefreshTime} from "../common/loading/loading.reducer";
import * as RouterActions from "../router/router.actions";
import {
    CreatePipelineAction,
    LoadAllPipelineInstancesAction,
    LoadPipelineBlueprints,
    UpdatePipelinePollingAction
} from "./pipelines.actions";
import {PipelinesState, selectPipelineList} from "./pipelines.reducer";
import {PipelineDataSource} from "./PipelineDataSource";
import {MatPaginator, MatSort} from "@angular/material";
import "../style/global-table-styles.css";
import "../style/style.css";

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
                (onManualRelad)="reload()">
        </header-bar>

        <div fxFlexFill fxLayout="column" fxLayoutGap="15px" class="table-wrapper">
            <!--Search Field-->
            <mat-form-field fxFlex class="search-position">
                <input matInput (keyup)="applyFilter($event.target.value)"
                       placeholder="{{'GENERAL.FILTER' | translate}}">
            </mat-form-field>

            <!--Resources Table-->
            <table matSort mat-table fxFlex="95%" [dataSource]="dataSource" class="mat-elevation-z8">
                <ng-container matColumnDef="health">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
                    <td mat-cell *matCellDef="let pipelineTableModel">
                        <resource-health [health]="pipelineTableModel.health"></resource-health>
                    </td>
                </ng-container>

                <ng-container matColumnDef="name">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Name</th>
                    <td mat-cell *matCellDef="let pipelineTableModel">
                        {{pipelineTableModel.name}}
                    </td>
                </ng-container>

                <ng-container matColumnDef="uuid">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
                    <td mat-cell *matCellDef="let pipelineTableModel">
                        {{pipelineTableModel.uuid}}
                    </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="['health', 'uuid', 'name']"></tr>
                <tr mat-row *matRowDef="let row; columns: ['health', 'uuid', 'name'];" class="example-element-row"></tr>
            </table>
        </div>
    `
})

export class PipelinesComponent implements OnDestroy, OnInit, AfterViewInit {
    public isLoading$: Observable<boolean>;
    public refreshTime$: Observable<number>;
    public title: string = "Pipelines";
    dataSource: PipelineDataSource = new PipelineDataSource(this.store.pipe(select(selectPipelineList)));

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    constructor(private store: Store<PipelinesState>) {

    }

    public ngOnInit(){
        this.isLoading$ = this.store.pipe(select(isSpinnerShowing));
        this.refreshTime$ = this.store.pipe(select(selectRefreshTime));
        this.store.dispatch(new UpdatePipelinePollingAction(true));
        this.store.dispatch(new LoadPipelineBlueprints());
    }

    public ngOnDestroy() {
        this.store.dispatch(new UpdatePipelinePollingAction(false));
    }

    public ngAfterViewInit() {
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
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

    applyFilter(filterValue: string) {
        this.dataSource.filter = filterValue;
        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage()
        }
    }
}
