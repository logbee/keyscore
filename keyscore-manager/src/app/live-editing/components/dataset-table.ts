import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {DatasetDataSource} from "../../dataSources/DatasetDataSource";
import {selectExtractedDatasets, selectExtractFinish} from "../live-editing.reducer";
import {select, Store} from "@ngrx/store";
import {BehaviorSubject} from "rxjs/index";
import {filter, take} from "rxjs/internal/operators";
import {MatPaginator, MatSort} from "@angular/material";

@Component({
    selector: "dataset-table",
    template: `
        <div fxFlexFill="" fxLayoutGap="15px" fxLayout="column">
            <!--Search Field-->
            <mat-form-field fxFlex="15%" class="search-position">
                <input matInput (keyup)="applyFilter($event.target.value)"
                       placeholder="{{'GENERAL.FILTER' | translate}}">
            </mat-form-field>
            
            <!--Dataset Datatable-->
            
            <table fxFlex="85%" mat-table [dataSource]="dataSource"
                   class="mat-elevation-z8 table-position">
                <ng-container matColumnDef="fields">
                    <th mat-header-cell *matHeaderCellDef>Fields</th>
                    <td mat-cell *matCellDef="let record">{{record.field.name}}</td>
                </ng-container>

                <ng-container matColumnDef="values">
                    <th mat-header-cell *matHeaderCellDef>Values</th>
                    <td mat-cell *matCellDef="let record">{{record.field.value.value}}</td>
                </ng-container>

                <ng-container matColumnDef="jsonClass">
                    <th mat-header-cell *matHeaderCellDef>ValueType</th>
                    <td mat-cell *matCellDef="let record">{{record.field.value.jsonClass}}</td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="[ 'jsonClass', 'fields', 'values']"></tr>
                <tr mat-row *matRowDef="let row; columns: [ 'jsonClass', 'fields', 'values']"></tr>
            </table>
        </div>
    `
})

export class DatasetTable implements AfterViewInit {
    private datasets$ = this.store.pipe(select(selectExtractedDatasets));
    private index: BehaviorSubject<number> = new BehaviorSubject<number>(0);
    private dataSource: DatasetDataSource;

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;


    constructor(private store: Store<any>) {
        this.store.pipe(select(selectExtractFinish), filter(extractFinish => extractFinish), take(1)).subscribe(_ =>
            this.dataSource = new DatasetDataSource(this.datasets$, this.index.asObservable()));
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

}