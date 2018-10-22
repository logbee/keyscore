import {Component, ViewChild} from "@angular/core";
import {DatasetDataSource} from "../../dataSources/DatasetDataSource";
import {selectDatasetsModels, selectExtractFinish, selectResultAvailable} from "../live-editing.reducer";
import {select, Store} from "@ngrx/store";
import {BehaviorSubject, Observable} from "rxjs/index";
import {filter, skip, skipUntil, take, takeWhile} from "rxjs/internal/operators";
import {MatPaginator, MatSort} from "@angular/material";
import {ChangeType, DatasetTableModel} from "../../models/dataset/DatasetTableModel";
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

@Component({
    selector: "dataset-table",
    template: `
        <div fxFlexFill="" fxLayoutGap="5px" fxLayout="column">
            <div fxFlexFill="" fxFlex="4" fxLayout="row" fxLayoutGap="15px">
                <!--Search Field-->
                <mat-form-field fxFlex="90" class="search-position">
                    <input matInput (keyup)="applyFilter($event.target.value)"
                           placeholder="{{'GENERAL.SEARCH' | translate}}">
                </mat-form-field>
                <navigation-control [index]="index.getValue()"
                                    [length]="(datasets$ | async).length"
                                    (counterEvent)="updateCounter($event)" fxFlex="">
                </navigation-control>
            </div>
            
            <div *ngIf="resultAvailable" fxFlex="4"  class="preset-margin">
                <div fxLayout="row" fxFlexFill="" fxLayoutGap="15px" fxLayoutAlign="end">
                    <button matTooltip="{{'FILTERLIVEEDITINGCOMPONENT.PRESET_IN' | translate}}" fxFlex="1" mat-icon-button (click)="changeViewPreset('showOnlyInput')">
                        <mat-icon>border_left</mat-icon>
                    </button>

                    <button  matTooltip="{{'FILTERLIVEEDITINGCOMPONENT.PRESET_ALL' | translate}}" fxFlex="1" mat-icon-button (click)="changeViewPreset('showEverything')">
                        <mat-icon>border_vertical</mat-icon>
                    </button>
                    
                    <button  matTooltip="{{'FILTERLIVEEDITINGCOMPONENT.PRESET_OUT' | translate}}" fxFlex="1" mat-icon-button (click)="changeViewPreset('showOnlyOutput')">
                        <mat-icon>border_right</mat-icon>
                    </button>
                </div>
            </div>

            <!--Dataset Datatable-->
            <table fxFlex="" mat-table matSort [dataSource]="dataSource"
                   class="mat-elevation-z8 table-position">
                <ng-container matColumnDef="fields">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>{{'FILTERLIVEEDITINGCOMPONENT.FIELDS' | translate}}</th>
                    <td mat-cell *matCellDef="let row"
                        [class.highlight-added]="row.input.change === 'added'"
                        [class.highlight-modified]="row.output.change === 'modified'"
                        [class.highlight-unchanged]="row.output.change === 'unchanged'"
                        [class.highlight-deleted]="row.output.change === 'deleted'">{{row?.input.name}}
                    </td>
                </ng-container>

                <ng-container matColumnDef="inValues">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>{{'FILTERLIVEEDITINGCOMPONENT.INPUT' | translate}}</th>
                    <td mat-cell *matCellDef="let row"
                        [class.highlight-added]="row.input.change === 'added'"
                        [class.highlight-modified]="row.output.change === 'modified'"
                        [class.highlight-unchanged]="row.output.change === 'unchanged'"
                        [class.highlight-deleted]="row.output.change === 'deleted'">
                        {{accessFieldValues(row?.input.value)}}
                    </td>
                </ng-container>

                <ng-container matColumnDef="outValues">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>{{'FILTERLIVEEDITINGCOMPONENT.OUTPUT' | translate}}</th>
                    <td mat-cell *matCellDef="let row"
                        [class.highlight-added]="row.input.change === 'added'"
                        [class.highlight-modified]="row.output.change === 'modified'"
                        [class.highlight-unchanged]="row.output.change === 'unchanged'"
                        [class.highlight-deleted]="row.output.change === 'deleted'">
                        <mat-label>{{accessFieldValues(row?.output?.value)}}</mat-label>
                    </td>

                </ng-container>

                <ng-container matColumnDef="jsonClass">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>ValueType</th>
                    <td mat-cell *matCellDef="let row"
                        [class.highlight-added]="row.input.change === 'added'"
                        [class.highlight-modified]="row.output.change === 'modified'"
                        [class.highlight-unchanged]="row.output.change === 'unchanged'"
                        [class.highlight-deleted]="row.output.change === 'deleted'">
                        <value-type [type]="row.input.value.jsonClass"></value-type>
                    </td>
                </ng-container>

                <!--<ng-container matColumnDef="divider">-->
                <!--<th mat-header-cell *matHeaderCellDef>Divider</th>-->
                <!--<td mat-cell *matCellDef="let row">-->
                <!--<mat-label></mat-label>-->
                <!--</td>-->
                <!--</ng-container>-->

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
            </table>
        </div>
    `
})

export class DatasetTable {
    private datasets$: Observable<DatasetTableModel[]> = this.store.pipe(select(selectDatasetsModels));
    private index: BehaviorSubject<number> = new BehaviorSubject<number>(0);
    private dataSource: DatasetDataSource;
    private resultAvailable: boolean = false;
    private displayedColumns: string[] = ['jsonClass', 'fields', 'inValues'];

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;


    constructor(private store: Store<any>) {
        this.store.pipe(select(selectExtractFinish), filter(extractFinish => extractFinish), take(1)).subscribe(_ => {
            this.dataSource = new DatasetDataSource(this.datasets$, this.index.asObservable());
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
        });

        this.store.pipe(select(selectResultAvailable), skip(1)).subscribe(_ => {
            // this.displayedColumns.push('divider');
            this.displayedColumns.push('outValues');
            this.resultAvailable = true;
        })
    }

    applyFilter(filterValue: string) {
        this.dataSource.filter = filterValue;
        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage()
        }
    }

    private updateCounter(count: number) {
        this.index.next(count);
    }

    accessFieldValues(valueObject: Value): any {
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

    changeViewPreset(value:string) {
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




        // if (this.displayedColumns.find(elem => elem == "outValues")) {
        //     this.displayedColumns = ['jsonClass', 'fields', 'inValues'];
        // } else {
        //     this.displayedColumns.push('outValues');
        // }
    }
}