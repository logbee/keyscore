import {Component, ViewChild} from "@angular/core";
import {DatasetDataSource} from "../../dataSources/DatasetDataSource";
import {selectExtractedDatasets, selectExtractFinish} from "../live-editing.reducer";
import {select, Store} from "@ngrx/store";
import {BehaviorSubject} from "rxjs/index";
import {filter, take} from "rxjs/internal/operators";
import {MatPaginator, MatSort} from "@angular/material";
import {UpdateDatasetCounter} from "../live-editing.actions";

@Component({
    selector: "dataset-table",
    template: `
        <div fxFlexFill="" fxLayoutGap="15px" fxLayout="column">
            <div fxFlexFill="" fxFlex="15%" fxLayout="row" fxLayoutGap="15px">
                <!--Search Field-->
                <mat-form-field fxFlex="70%" class="search-position">
                    <input matInput (keyup)="applyFilter($event.target.value)"
                           placeholder="{{'GENERAL.FILTER' | translate}}">
                </mat-form-field>    
                <navigation-control [index]="index.getValue()"
                                    [length]="(datasets$ | async).length"
                                    (counterEvent)="updateCounter($event)" fxFlex="">
                </navigation-control>
            </div>
            
            
            <!--Dataset Datatable-->
            <table fxFlex="85%" mat-table matSort [dataSource]="dataSource"
                   class="mat-elevation-z8 table-position">
                <ng-container matColumnDef="fields">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Fields</th>
                    <td mat-cell *matCellDef="let record">{{record.field.name}}</td>
                </ng-container>

                <ng-container matColumnDef="values">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Values</th>
                    <td mat-cell *matCellDef="let record">{{record.field.value.value}}</td>
                </ng-container>

                <ng-container matColumnDef="jsonClass">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>ValueType</th>
                    <td mat-cell *matCellDef="let record">
                        <value-type [type]="record.field.value.jsonClass"></value-type>
                    </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="[ 'jsonClass', 'fields', 'values']"></tr>
                <tr mat-row *matRowDef="let row; columns: [ 'jsonClass', 'fields', 'values']"></tr>
            </table>
        </div>
    `
})

export class DatasetTable {
    private datasets$ = this.store.pipe(select(selectExtractedDatasets));
    private index: BehaviorSubject<number> = new BehaviorSubject<number>(0);
    private dataSource: DatasetDataSource;

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;


    constructor(private store: Store<any>) {
        this.store.pipe(select(selectExtractFinish), filter(extractFinish => extractFinish), take(1)).subscribe(_ => {
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
}