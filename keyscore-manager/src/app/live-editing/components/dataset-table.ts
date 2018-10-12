import {Component, ViewChild} from "@angular/core";
import {DatasetDataSource} from "../../dataSources/DatasetDataSource";
import {selectDatasets, selectExtractFinish} from "../live-editing.reducer";
import {select, Store} from "@ngrx/store";
import {BehaviorSubject, Observable} from "rxjs/index";
import {filter, take} from "rxjs/internal/operators";
import {MatPaginator, MatSort} from "@angular/material";
import {UpdateDatasetCounter} from "../live-editing.actions";
import {DatasetTableModel} from "../../models/dataset/DatasetTableModel";
import {
    BooleanValue, DecimalValue,
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
            <div fxFlexFill="" fxFlex="10%" fxLayout="row" fxLayoutGap="15px">
                <!--Search Field-->
                <mat-form-field fxFlex="90%" class="search-position">
                    <input matInput (keyup)="applyFilter($event.target.value)"
                           placeholder="{{'GENERAL.FILTER' | translate}}">
                </mat-form-field>    
                <navigation-control [index]="index.getValue()"
                                    [length]="(datasets$ | async).length"
                                    (counterEvent)="updateCounter($event)" fxFlex="">
                </navigation-control>
            </div>
            
            
            <!--Dataset Datatable-->
            <table fxFlex="" mat-table matSort [dataSource]="dataSource"
                   class="mat-elevation-z8 table-position">
                <ng-container matColumnDef="fields">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Fields</th>
                    <td mat-cell *matCellDef="let row">{{row.input.name}}</td>
                </ng-container>

                <ng-container matColumnDef="inputvalues">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Input Values</th>
                    <td mat-cell *matCellDef="let row">{{accessFieldValues(row.input.value)}}</td>
                </ng-container>

                <ng-container matColumnDef="outputvalues">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Output Values</th>
                    <td mat-cell *matCellDef="let row">{{accessFieldValues(row.output?.value)}}</td>
                </ng-container>
                
                <ng-container matColumnDef="jsonClass">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>ValueType</th>
                    <td mat-cell *matCellDef="let row">
                        <value-type [type]="row.input.value.jsonClass"></value-type>
                    </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="[ 'jsonClass', 'fields', 'inputvalues', 'outputvalues']"></tr>
                <tr mat-row *matRowDef="let row; columns: [ 'jsonClass', 'fields', 'inputvalues', 'outputvalues']"></tr>
            </table>
        </div>
    `
})

export class DatasetTable {
    private datasets$: Observable<DatasetTableModel[]> = this.store.pipe(select(selectDatasets));
    private index: BehaviorSubject<number> = new BehaviorSubject<number>(0);
    private dataSource: DatasetDataSource;

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;


    constructor(private store: Store<any>) {
        this.store.pipe(select(selectExtractFinish), filter(extractFinish => extractFinish), take(1)).subscribe(_ => {
            console.log("Initializing datasource");
            this.dataSource = new DatasetDataSource(this.datasets$, this.index.asObservable());
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
        });
    }

    applyFilter(filterValue: string) {
        this.dataSource.filter = filterValue;
        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage()
        }
    }

    private updateCounter(count: number) {
        console.log("Count emitted" + count);
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
}