import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {BehaviorSubject, Observable} from "rxjs";
import {DatasetTableModel} from "../../models/dataset/DatasetTableModel";
import {select, Store} from "@ngrx/store";
import {DatasetDataSource} from "../../data-source/dataset-data-source";
import {MatPaginator, MatSort} from "@angular/material";
import {filter, take} from "rxjs/operators";
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
import {getDatasetModels, getExtractFinish} from "../index";

@Component({
    selector: "data-table",
    template: `

        <!--Search Field-->
        <div fxLayout="column" fxFlex="100" fxLayoutGap="15px">
            <mat-form-field fxFlex="25" style="margin-left: 15px;">
                <input matInput (keyup)="applyFilterPattern($event.target.value)"
                       placeholder="{{'GENERAL.SEARCH' | translate}}">
                <button mat-button matSuffix mat-icon-button aria-label="Search">
                    <mat-icon>search</mat-icon>
                </button>
            </mat-form-field>

            <table fxFlex mat-table matSort [dataSource]="dataSource" class="table-position">
                <ng-container matColumnDef="fields">
                    <th class="text-padding" mat-header-cell *matHeaderCellDef mat-sort-header>
                        {{'FILTERLIVEEDITINGCOMPONENT.FIELDS' | translate}}
                    </th>
                    <td mat-cell class="text-padding" *matCellDef="let row">{{row?.input.name}}</td>
                </ng-container>

                <ng-container matColumnDef="outValues">
                    <th mat-header-cell class="text-padding" mat-sort-header *matHeaderCellDef>
                        {{'FILTERLIVEEDITINGCOMPONENT.OUTPUT' | translate}}
                    </th>
                    <td mat-cell class="cell-border" *matCellDef="let row">
                        <mat-label>{{accessFieldValues(row?.output?.value)}}</mat-label>
                    </td>

                </ng-container>

                <ng-container matColumnDef="jsonClass">
                    <th class="text-padding" mat-header-cell *matHeaderCellDef mat-sort-header style="max-width: 15%">
                        ValueType
                    </th>
                    <td mat-cell class="text-padding" *matCellDef="let row">
                        <value-type [type]="row.input.value.jsonClass"></value-type>
                    </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
            </table>
        </div>
    `
})

export class DatatableComponent implements AfterViewInit {
    private datasets$: Observable<DatasetTableModel[]> = this.store.pipe(select(getDatasetModels));
    private dataSource: DatasetDataSource;
    private datasets: DatasetTableModel[];

    private datasetIndex: BehaviorSubject<number> = new BehaviorSubject<number>(0);

    private recordsIndex: BehaviorSubject<number> = new BehaviorSubject<number>(0);
    private displayedColumns: string[] = ['jsonClass', 'fields', 'outValues'];
    private currentDataset: DatasetTableModel;

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;


    constructor(private store: Store<any>) {
        this.datasets$.subscribe(datasets => {
            this.datasets = datasets;
            this.currentDataset = datasets[this.datasetIndex.getValue()];
        });
        this.store.pipe(select(getExtractFinish), filter(extractFinish => extractFinish), take(1)).subscribe(_ => {
            this.dataSource = new DatasetDataSource(this.datasets$, this.datasetIndex.asObservable(), this.recordsIndex.asObservable());
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
        });
        // this.store.dispatch(new StoreCurrentDataset(this.datasets[this.datasetIndex.getValue()]));
        // this.store.dispatch(new StoreCurrentRecordIndex(this.recordsIndex.getValue()));
    }

    ngAfterViewInit() {

    }

    applyFilter(filterValue: string) {
        this.dataSource.filter = filterValue;
        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage()
        }
    }

    private updateDatasetCounter(count: number) {
        this.datasetIndex.next(count);
        // this.store.dispatch(new StoreCurrentDataset(this.datasets[count]));
    }

    private updateRecordCounter(count: number) {
        this.recordsIndex.next(count);
        // this.store.dispatch(new StoreCurrentRecordIndex(this.recordsIndex.getValue()));
    }

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

    applyFilterPattern(filterValue: string) {
        this.dataSource.filter = filterValue;
        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage()
        }
    }
}