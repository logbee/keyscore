import {Component, Input, OnInit, ViewChild} from "@angular/core";
import {BehaviorSubject, combineLatest, Observable} from "rxjs";
import {DatasetTableModel} from "../../models/dataset/DatasetTableModel";
import {select, Store} from "@ngrx/store";
import {DatasetDataSource} from "../../data-source/dataset-data-source";
import {MatPaginator, MatSort} from "@angular/material";
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
import {getDatasetModels} from "../index";

@Component({
    selector: "data-table",
    template: `
        <!--Search Field-->
        <div fxLayout="column" fxLayoutGap="15px">
            <div fxLayout="row"  fxFlex="70">
                <mat-form-field style="margin-left: 15px;" fxFlex="33">
                    <input matInput (keyup)="applyFilterPattern($event.target.value)"
                           placeholder="{{'GENERAL.SEARCH' | translate}}">
                    <button mat-button matSuffix mat-icon-button aria-label="Search">
                        <mat-icon>search</mat-icon>
                    </button>
                </mat-form-field>
                <!--Datasets-->
                <left-right-control  fxFlex="15" [index]="datasetIndex.getValue()"
                                                 [length]="dataSource$.getValue().getNumberOfDatsets()"
                                                 [label]="datasetLabel"
                                                 (counterEvent)="updateDatasetCounter($event)" style="padding-left:15px; margin-top: 15px">
                </left-right-control>
                <!--Records-->
                <left-right-control  fxFlex
                                     [index]="recordsIndex.getValue()"
                                     [length]="dataSource$.getValue().getNumberOfRecords()"
                                     [label]="recordLabel"
                                     (counterEvent)="updateRecordCounter($event)" style="padding-left:15px; margin-top: 15px">
                </left-right-control>
            </div>
            

            <table fxFlex="75" mat-table matSort [dataSource]="dataSource$.getValue()" class="table-position">
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

export class DatatableComponent implements OnInit {
    private datasetTableModels$: Observable<Map<string, DatasetTableModel[]> >= this.store.pipe(select(getDatasetModels));
    private dataSource$: BehaviorSubject<DatasetDataSource> = new BehaviorSubject<DatasetDataSource>(new DatasetDataSource(new Map(), 0, 0, ""));
    private datasetTableModels: Map<string, DatasetTableModel[]>;

    private datasetIndex: BehaviorSubject<number> = new BehaviorSubject<number>(0);

    private recordsIndex: BehaviorSubject<number> = new BehaviorSubject<number>(0);
    private displayedColumns: string[] = ['jsonClass', 'fields', 'outValues'];
    private currentDataset: DatasetTableModel;

    private datasetLabel: string = "Datasets";
    private recordLabel: string = "Records";

    @Input('selectedBlock') set selectedBlock(selectedBlock: string) {
        this.selectedBlock$.next(selectedBlock)
    };
    private selectedBlock$ = new BehaviorSubject<string>("");

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;


    constructor(private store: Store<any>) {

    }

    ngOnInit() {
        this.datasetTableModels$.subscribe(models => {
            this.datasetTableModels = models;
            this.currentDataset = this.datasetTableModels[this.datasetIndex.getValue()];
        });

        combineLatest(this.datasetTableModels$, this.datasetIndex.asObservable(), this.recordsIndex.asObservable(), this.selectedBlock$).subscribe(([datasetTableModels, index, recordsIndex, selectedBlock]) => {
            console.log("TEST");
            this.dataSource$.next(new DatasetDataSource(datasetTableModels, index, recordsIndex, selectedBlock));

            this.dataSource$.getValue().paginator = this.paginator;
            this.dataSource$.getValue().sort = this.sort;
        });
    }



    applyFilter(filterValue: string) {
        this.dataSource$.getValue().filter = filterValue;
        if (this.dataSource$.getValue().paginator) {
            this.dataSource$.getValue().paginator.firstPage()
        }
    }

    private updateDatasetCounter(count: number) {
        console.log("Counter");
        this.datasetIndex.next(count);
    }

    private updateRecordCounter(count: number) {
        console.log("Counter");

        this.recordsIndex.next(count);
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
        this.dataSource$.getValue().filter = filterValue;
        if (this.dataSource$.getValue().paginator) {
            this.dataSource$.getValue().paginator.firstPage()
        }
    }
}