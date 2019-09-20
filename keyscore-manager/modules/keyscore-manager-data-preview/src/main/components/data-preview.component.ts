import {Component, Input, OnInit, ViewChild} from "@angular/core";
import {BehaviorSubject, combineLatest} from "rxjs";
import {DatasetDataSource} from "../dataset-data-source";
import {MatPaginator, MatSort} from "@angular/material";
import {Value, ValueJsonClass} from "@keyscore-manager-models/src/main/dataset/Value";
import { Dataset } from "@keyscore-manager-models/src/main/dataset/Dataset";

@Component({
    selector: "data-preview",
    template: `
        <!--Search Field-->
        <div fxLayout="column" fxLayoutGap="15px">
            <div fxLayout="row" fxFlex="70">
                <mat-form-field fxFlex="33">
                    <input matInput (keyup)="applyFilterPattern($event.target.value)"
                           placeholder="{{'GENERAL.SEARCH' | translate}}">
                    <button mat-button matSuffix mat-icon-button aria-label="Search">
                        <mat-icon>search</mat-icon>
                    </button>
                </mat-form-field>
                <!--Datasets-->
                <left-right-control class="control" fxFlex="15" 
                                    [index]="datasetIndex.getValue()"
                                    [length]="dataSource$.getValue().getNumberOfDatsets()"
                                    [label]= labelDatasets
                                    (counterEvent)="updateDatasetCounter($event)">
                </left-right-control>
                <!--Records-->
                <left-right-control class="control" fxFlex
                                    [index]="recordsIndex.getValue()"
                                    [length]="dataSource$.getValue().getNumberOfRecords()"
                                    [label]= labelRecords
                                    (counterEvent)="updateRecordCounter($event)">
                </left-right-control>

                <button class="switch" mat-raised-button color="primary"
                        [matTooltip]="where === 'after' ? 'Datasets after the transformation.' : 'Datasets before the transformation.'"
                        (click)="switch()">
                    {{ where === "before" ? ('DATATABLE.INSWITCH' | translate) : ('DATATABLE.OUTSWITCH' | translate)}}
                </button>
            </div>


            <table fxFlex="75" mat-table matSort [dataSource]="dataSource$.getValue()" class="table-position">
                <ng-container matColumnDef="fields">
                    <th class="text-padding" mat-header-cell *matHeaderCellDef mat-sort-header>
                        {{'DATATABLE.FIELDS' | translate}}
                    </th>
                    <td mat-cell class="text-padding" *matCellDef="let row">{{row?.input.name}}</td>
                </ng-container>

                <ng-container matColumnDef="outValues">
                    <th mat-header-cell class="text-padding" mat-sort-header *matHeaderCellDef>
                        {{'DATATABLE.DATASETS' | translate}}
                    </th>
                    <td mat-cell class="cell-border" *matCellDef="let row">
                        <mat-label>{{accessFieldValues(row?.output?.value)}}</mat-label>
                    </td>

                </ng-container>

                <ng-container matColumnDef="jsonClass">
                    <th class="text-padding" mat-header-cell *matHeaderCellDef mat-sort-header>
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
    `,
    styleUrls:['./data-preview.component.scss']
})

export class DataPreviewComponent implements OnInit {

    //Currently selected Block's uuid
    @Input('selectedBlock') set selectedBlock(selectedBlock: string) {
        this.selectedBlock$.next(selectedBlock);
    };

    //BehaviourSubject for selectedBlock's uuid
    private selectedBlock$ = new BehaviorSubject<string>("");


    @Input('inputDatasets') set inputTableModels(value: Map<string, Dataset[]>) {
        this.selectedModels$.next(value);
        this.inputDatasets = value;
        // console.log("InputDatasets input:" + JSON.stringify(this.inputDatasets));
        // console.log("OutDatasets output:" + JSON.stringify(this.outputDatasets));
    }

    private inputDatasets: Map<string, Dataset[]>;

    @Input('outputDatasets')
    private outputDatasets: Map<string, Dataset[]>;

    // Behaviour Subject for selected TableModels; Input for creating the datasource
    private selectedModels$: BehaviorSubject<Map<string, Dataset[]>> = new BehaviorSubject<Map<string, Dataset[]>>(undefined);
    // datasetIndex indicates which Dataset to display
    private datasetIndex: BehaviorSubject<number> = new BehaviorSubject<number>(0);
    // datasetIndex indicates which Record to display
    private recordsIndex: BehaviorSubject<number> = new BehaviorSubject<number>(0);
    // specifying the visible columns
    private displayedColumns: string[] = ['jsonClass', 'fields', 'outValues'];
    // indicating if in or outputdatasets are required default value is after
    private where: string = "after";
    //Labels
    private labelDatasets = "Datasets";
    private labelRecords = "Records";

    //Angular Material DataTable
    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    //Initializing empty Datasource
    private dataSource$: BehaviorSubject<DatasetDataSource> = new BehaviorSubject<DatasetDataSource>(new DatasetDataSource(new Map(), 0, 0, "", ""));

    ngOnInit() {
        this.selectedModels$.next(this.outputDatasets);

        combineLatest(this.selectedModels$, this.datasetIndex.asObservable(), this.recordsIndex.asObservable(), this.selectedBlock$).subscribe(([datasets, index, recordsIndex, selectedBlock]) => {
            this.dataSource$.next(new DatasetDataSource(datasets, index, recordsIndex, selectedBlock, this.where));
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
        this.datasetIndex.next(count);
    }

    private updateRecordCounter(count: number) {
        this.recordsIndex.next(count);
    }

    private accessFieldValues(valueObject: Value): any {
        if (!valueObject) {
            return "No values to access!"
        } else {
            switch (valueObject.jsonClass) {
                case ValueJsonClass.BooleanValue:
                case ValueJsonClass.DecimalValue:
                case ValueJsonClass.NumberValue:
                case ValueJsonClass.TextValue: {
                    return valueObject.value.toString();
                }
                case ValueJsonClass.TimestampValue:
                case ValueJsonClass.DurationValue: {
                    return valueObject.seconds.toString();
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

    // TODO: Redo
    switch() {
        if (this.where === "before") {
            this.selectedModels$.next(this.outputDatasets);
            this.where = "after";
        } else if (this.where === "after") {
            this.selectedModels$.next(this.inputDatasets);
            this.where = "before";
        }
    }
}