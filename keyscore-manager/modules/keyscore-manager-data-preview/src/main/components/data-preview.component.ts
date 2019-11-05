import {Component, Input, OnInit, ViewChild} from "@angular/core";
import {BehaviorSubject} from "rxjs";
import {DatasetDataSource} from "../dataset-data-source";
import {MatSort} from "@angular/material";
import {Value, ValueJsonClass} from "@keyscore-manager-models/src/main/dataset/Value";
import {Dataset} from "@keyscore-manager-models/src/main/dataset/Dataset";

@Component({
    selector: "data-preview",
    template: `
        <!--Search Field-->
        <div class="data-preview-wrapper" fxLayout="column">
            <div fxLayout="row" fxLayoutAlign="space-between center" fxFlex>
                <div fxLayout="row" fxLayoutGap="15px" fxFlex fxLayoutAlign="start center">
                    <mat-form-field fxFlex="33">
                        <input matInput (keyup)="applyFilterPattern($event.target.value)"
                               placeholder="{{'GENERAL.SEARCH' | translate}}">
                        <button mat-button matSuffix mat-icon-button aria-label="Search">
                            <mat-icon>search</mat-icon>
                        </button>
                    </mat-form-field>

                    <!--Datasets-->
                    <left-right-control class="control" fxFlexAlign="start"
                                        [index]="dataSource?.datasetIndex + 1"
                                        [length]="dataSource?.numberOfDatasets"
                                        [label]=labelDatasets
                                        (counterEvent)="updateDatasetCounter($event)">
                    </left-right-control>
                    <!--Records-->
                    <left-right-control fxFlexAlign="start" class="control"
                                        [index]="dataSource?.recordsIndex + 1"
                                        [length]="dataSource?.numberOfRecords"
                                        [label]=labelRecords
                                        (counterEvent)="updateRecordCounter($event)">
                    </left-right-control>
                </div>

                <div fxLayout="row" fxLayoutAlign="start center" fxLayoutGap="15px">
                    <span style="font-size: small">
                        {{ ('DATATABLE.INSWITCH' | translate) }}
                    </span>
                    <mat-slide-toggle class="mat-colored-always" color="primary"
                                      [checked]="_inputOutputChecked"
                                      (click)="switch()">

                    </mat-slide-toggle>
                    <span style="font-size: small">
                        {{ ('DATATABLE.OUTSWITCH' | translate)}}
                    </span>
                </div>
            </div>

            <ng-container *ngIf="(dataAvailable|async); else banner">
                <table fxFlex="75" mat-table matSort [dataSource]="dataSource" class="table-position">
                    <ng-container matColumnDef="fields">
                        <th class="text-padding" mat-header-cell *matHeaderCellDef mat-sort-header>
                            {{'DATATABLE.FIELDS' | translate}}
                        </th>
                        <td mat-cell class="text-padding" *matCellDef="let row">{{row?.input.name}}</td>
                    </ng-container>

                    <ng-container matColumnDef="outValues">
                        <th mat-header-cell class="text-padding" mat-sort-header *matHeaderCellDef>
                            {{'DATATABLE.VALUES' | translate}}
                        </th>
                        <td mat-cell class="cell-border" *matCellDef="let row">
                            <mat-label>{{accessFieldValues(row?.output?.value)}}</mat-label>
                        </td>

                    </ng-container>

                    <ng-container matColumnDef="jsonClass">
                        <th class="text-padding" mat-header-cell *matHeaderCellDef mat-sort-header>
                            Type
                        </th>
                        <td mat-cell class="text-padding" *matCellDef="let row">
                            <value-type [type]="row.input.value.jsonClass"></value-type>
                        </td>
                    </ng-container>

                    <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                    <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
                </table>
            </ng-container>
        </div>

        <ng-template #banner>
            <div class="no-results-wrapper" fxFlexAlign="center">
                <span translate>GENERAL.NO_RESULTS</span>
            </div>
        </ng-template>
    `,
    styleUrls: ['./data-preview.component.scss']
})

export class DataPreviewComponent implements OnInit {

    //Labels
    readonly labelDatasets = "Dataset";
    readonly labelRecords = "Record";

    //Currently selected Block's uuid
    @Input('selectedBlock') set selectedBlock(val: string) {
        this._selectedBlock = val;
        if (this._allDatasets.get(this.where) && this._allDatasets.get(this.where).get(this.selectedBlock)) {
            this.dataSource.datasets = this._allDatasets.get(this.where).get(this.selectedBlock);
        }
    }

    get selectedBlock(): string {
        return this._selectedBlock;
    }

    private _selectedBlock: string;


    @Input() set inputDatasets(value: Map<string, Dataset[]>) {
        this._allDatasets.set('before', value);
        if (this.where === 'before') {
            this.dataSource.datasets = this._allDatasets.get(this.where).get(this.selectedBlock);
        }
    }

    @Input() set outputDatasets(value: Map<string, Dataset[]>) {
        this._allDatasets.set('after', value);
        if (this.where === 'after') {
            this.dataSource.datasets = this._allDatasets.get(this.where).get(this.selectedBlock);
        }
    };

    private _allDatasets: Map<'after' | 'before', Map<string, Dataset[]>> = new Map();


    //Behaviour Subject indicating if data for block can be shown
    private dataAvailable: BehaviorSubject<Boolean> = new BehaviorSubject<Boolean>(true);

    // specifying the visible columns
    private displayedColumns: string[] = ['jsonClass', 'fields', 'outValues'];
    // indicating if in or outputdatasets are required default value is after
    private where: 'after' | 'before' = 'after';

    private _inputOutputChecked = true;

    //Angular Material DataTable
    @ViewChild(MatSort) sort: MatSort;

    private dataSource: DatasetDataSource = new DatasetDataSource([]);

    ngOnInit(): void {
        this.dataSource.sort = this.sort;
    }

    private updateDatasetCounter(count: number) {
        this.dataSource.datasetIndex = count - 1;
    }

    private updateRecordCounter(count: number) {
        this.dataSource.recordsIndex = count - 1;
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
        this.dataSource.filter = filterValue;
    }

    switch() {
        if (this.where === "before") {
            this.where = "after";
        } else {
            this.where = "before";
        }
        this.dataSource.datasets = this._allDatasets.get(this.where).get(this.selectedBlock);
    }
}
