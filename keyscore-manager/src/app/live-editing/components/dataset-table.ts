import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {DatasetDataSource} from "../../dataSources/DatasetDataSource";
import {selectDatasetsModels, selectExtractFinish} from "../live-editing.reducer";
import {select, Store} from "@ngrx/store";
import {BehaviorSubject, Observable} from "rxjs/index";
import {filter, take} from "rxjs/internal/operators";
import {MatPaginator, MatSort} from "@angular/material";
import {DatasetTableModel} from "../../models/dataset/DatasetTableModel";
import {
    BooleanValue,
    DecimalValue,
    DurationValue,
    NumberValue,
    TextValue,
    TimestampValue,
    Value,
    ValueJsonClass
} from "../../models/dataset/Value";
import {Dataset} from "../../models/dataset/Dataset";

@Component({
    selector: "dataset-table",
    template: `
        <div fxFlexFill="" fxLayoutGap="5px" fxLayout="column">
            <div fxFlexFill="" fxFlex="5" fxLayout="row" fxLayoutGap="50px">
                <!--Search Field-->
                <mat-form-field fxFlex="55" class="search-position">
                    <input matInput (keyup)="applyFilter($event.target.value)"
                           placeholder="{{'GENERAL.SEARCH' | translate}}">
                    <button mat-button matSuffix mat-icon-button aria-label="Search">
                        <mat-icon>search</mat-icon>
                    </button>
                </mat-form-field>
                <leftTotRight-navigation-control [index]="index.getValue()"
                                                 [length]="(datasets$ | async).length"
                                                 (counterEvent)="updateDatasetCounter($event)" fxFlex="15">
                </leftTotRight-navigation-control>
                <topToBottom-navigation-control
                                                 [index]="recordsIndex.getValue()"
                                                 [length]="currentDataset.records.length"
                                                 (counterEvent)="updateRecordCounter($event)" fxFlex="15">
                </topToBottom-navigation-control>
                <div style="margin-right: 15px!important">
                 <filter-presets (preset)="adjustDisplayedColumns($event)" fxFlex=""></filter-presets>
                </div>
            </div>
              
            <!--Dataset Datatable-->
            <table fxFlex="" mat-table matSort [dataSource]="dataSource"
                   class="mat-elevation-z8 table-position live-editing">
                <ng-container matColumnDef="fields">
                    <th class="text-padding" mat-header-cell *matHeaderCellDef mat-sort-header>
                        {{'FILTERLIVEEDITINGCOMPONENT.FIELDS' | translate}}
                    </th>
                    <td mat-cell class="text-padding" *matCellDef="let row"
                        [class.highlight-added]="row.input.change === 'added'"
                        [class.highlight-modified]="row.output.change === 'modified'"
                        [class.highlight-unchanged]="row.output.change === 'unchanged'"
                        [class.highlight-deleted]="row.output.change === 'deleted'">{{row?.input.name}}
                    </td>
                </ng-container>

                <ng-container matColumnDef="inValues">
                    <th mat-header-cell class="text-padding" *matHeaderCellDef mat-sort-header>
                        {{'FILTERLIVEEDITINGCOMPONENT.INPUT' | translate}}
                    </th>
                    <td mat-cell class="cell-border" *matCellDef="let row"
                        [class.highlight-added]="row.input.change === 'added'"
                        [class.highlight-modified]="row.output.change === 'modified'"
                        [class.highlight-unchanged]="row.output.change === 'unchanged'"
                        [class.highlight-deleted]="row.output.change === 'deleted'">
                        {{accessFieldValues(row?.input.value)}}
                    </td>
                </ng-container>

                <ng-container matColumnDef="outValues">
                    <th mat-header-cell class="text-padding" *matHeaderCellDef mat-sort-header>
                        {{'FILTERLIVEEDITINGCOMPONENT.OUTPUT' | translate}}
                    </th>
                    <td mat-cell class="cell-border" *matCellDef="let row"
                        [class.highlight-added]="row.input.change === 'added'"
                        [class.highlight-modified]="row.output.change === 'modified'"
                        [class.highlight-unchanged]="row.output.change === 'unchanged'"
                        [class.highlight-deleted]="row.output.change === 'deleted'">
                        <mat-label>{{accessFieldValues(row?.output?.value)}}</mat-label>
                    </td>

                </ng-container>

                <ng-container matColumnDef="jsonClass">
                    <th class="text-padding" mat-header-cell *matHeaderCellDef mat-sort-header style="max-width: 15%">
                        ValueType
                    </th>
                    <td mat-cell class="text-padding" *matCellDef="let row"
                        [class.highlight-added]="row.input.change === 'added'"
                        [class.highlight-modified]="row.output.change === 'modified'"
                        [class.highlight-unchanged]="row.output.change === 'unchanged'"
                        [class.highlight-deleted]="row.output.change === 'deleted'">
                        <value-type [type]="row.input.value.jsonClass"></value-type>
                    </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
            </table>
        </div>
    `
})

export class DatasetTable implements AfterViewInit {
    private datasets$: Observable<DatasetTableModel[]> = this.store.pipe(select(selectDatasetsModels));
    private index: BehaviorSubject<number> = new BehaviorSubject<number>(0);
    private recordsIndex: BehaviorSubject<number> = new BehaviorSubject<number>(0);
    private dataSource: DatasetDataSource;
    private displayedColumns: string[] = ['jsonClass', 'fields', 'inValues', 'outValues'];
    private currentDataset: DatasetTableModel;
    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;


    constructor(private store: Store<any>) {
        this.datasets$.subscribe(datasets => this.currentDataset = datasets[this.index.getValue()]);
        this.store.pipe(select(selectExtractFinish), filter(extractFinish => extractFinish), take(1)).subscribe(_ => {

            this.dataSource = new DatasetDataSource(this.datasets$, this.index.asObservable(), this.recordsIndex.asObservable());
        });
    }

    ngAfterViewInit() {
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
    }

    applyFilter(filterValue: string) {
        this.dataSource.filter = filterValue;
        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage()
        }
    }

    private updateDatasetCounter(count: number) {
        this.index.next(count);
    }

    private updateRecordCounter(count: number) {
        console.log("triggerd record counter", count);
        this.recordsIndex.next(count)
    }

    private adjustDisplayedColumns(value: string) {
        switch (value) {
            case "showOnlyInput":
                this.displayedColumns = ['jsonClass', 'fields', 'inValues'];
                break;
            case "showEverything":
                this.displayedColumns = ['jsonClass', 'fields', 'inValues', 'outValues'];
                break;
            case "showOnlyOutput":
                this.displayedColumns = ['jsonClass', 'fields', 'outValues'];
                break;
        }
    }

    // noinspection JSMethodCanBeStatic
    private accessFieldValues(valueObject: Value): any {
        if (!valueObject) {
            return "No output yet!"
        } else {
            switch (valueObject.jsonClass) {
                case ValueJsonClass.BooleanValue: {
                    return (valueObject as BooleanValue).value.toString();
                }
                case ValueJsonClass.TextValue: {
                    return (valueObject as TextValue).value;
                }
                case ValueJsonClass.NumberValue: {
                    return (valueObject as NumberValue).value.toString();
                }
                case ValueJsonClass.DurationValue: {
                    return (valueObject as DurationValue).seconds.toString();

                }
                case ValueJsonClass.TimestampValue: {
                    return (valueObject as TimestampValue).seconds.toString();
                }
                case ValueJsonClass.DecimalValue: {
                    return (valueObject as DecimalValue).value.toString();
                }
                default: {
                    return "Unknown Type";
                }
            }
        }
    }
}