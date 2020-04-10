import {Component, Input, OnInit, ViewChild} from "@angular/core";
import {BehaviorSubject, combineLatest, Observable} from "rxjs";
import {DatasetDataSource} from "../dataset-data-source";
import { MatSort } from "@angular/material/sort";
import {Value, ValueJsonClass} from "@keyscore-manager-models/src/main/dataset/Value";
import {Dataset} from "@keyscore-manager-models/src/main/dataset/Dataset";
import {map} from "rxjs/operators";

@Component({
    selector: "data-preview",
    template: `
        <!--Search Field-->
        <div class="data-preview-wrapper" fxLayout="column">
            <mat-progress-bar mode="indeterminate"
                              *ngIf="(isLoadingDatasets$|async) && dataSource.numberOfDatasets && (!(loadingError$|async))"
                              class="progress"></mat-progress-bar>
            <div fxLayout="row" fxLayoutAlign="space-between start">
                <div fxLayout="row" fxLayoutGap="15px" fxFlex fxLayoutAlign="start center">
                    <mat-form-field fxFlex="33">
                        <input matInput (keyup)="applyFilterPattern($event.target.value)"
                               placeholder="{{'GENERAL.TO_SEARCH' | translate}}">
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

                <div fxLayout="row" fxFlexAlign="center" fxLayoutAlign="start center" fxLayoutGap="15px">
                    <span style="font-size: small">
                        {{ ('DATATABLE.INSWITCH' | translate) }}
                    </span>
                    <mat-slide-toggle class="mat-colored-always" color="primary"
                                      [checked]="isOutputView"
                                      (click)="switch()">

                    </mat-slide-toggle>
                    <span style="font-size: small">
                        {{ ('DATATABLE.OUTSWITCH' | translate)}}
                    </span>
                </div>
            </div>

            <ng-container>
                <div class="overlay"
                     *ngIf="(isLoadingDatasets$|async) && (!dataSource.numberOfDatasets || (loadingError$|async))">
                    <div class="spinner-wrapper">
                        <mat-progress-spinner [diameter]="75" mode="indeterminate">
                        </mat-progress-spinner>
                        <span *ngIf="loadingError$|async" translate>DATATABLE.EXTRACT_ERROR</span>
                    </div>

                </div>
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
                            <value-type [value]="row.input.value"></value-type>
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

    @Input() set isLoadingDatasetsAfter(val: boolean) {
        this.isLoadingDatasetsAfter$.next(val);
    }

    @Input() set loadingErrorAfter(val: boolean) {
        this.loadingErrorAfter$.next(val);
        if (this.where === 'after') {
            this._allDatasets = new Map();
            this.dataSource.datasets = [];
        }
    }

    @Input() set isLoadingDatasetsBefore(val: boolean) {
        this.isLoadingDatasetsBefore$.next(val);
    }

    @Input() set loadingErrorBefore(val: boolean) {
        this.loadingErrorBefore$.next(val);
        if (this.where === "before") {
            this._allDatasets = new Map();
            this.dataSource.datasets = [];
        }
    }


    isLoadingDatasetsAfter$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    loadingErrorAfter$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    isLoadingDatasetsBefore$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    loadingErrorBefore$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    inputOutputToggled$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);

    loadingError$: Observable<boolean> =
        combineLatest([this.loadingErrorBefore$, this.loadingErrorAfter$, this.inputOutputToggled$])
            .pipe(
                map(([beforeError, afterError, _]) => this.where === 'after' ? afterError : beforeError)
            );
    isLoadingDatasets$: Observable<boolean> =
        combineLatest([this.isLoadingDatasetsBefore$, this.isLoadingDatasetsAfter$, this.inputOutputToggled$])
            .pipe(
                map(([beforeIsLoading, afterIsLoading, _]) => this.where === 'after' ? afterIsLoading : beforeIsLoading)
            );

    dataSource: DatasetDataSource = new DatasetDataSource([]);
    displayedColumns: string[] = ['jsonClass', 'fields', 'outValues'];
    where: 'after' | 'before' = 'after';
    isOutputView = true;

    //Angular Material DataTable
    @ViewChild(MatSort, { static: true }) sort: MatSort;


    ngOnInit(): void {
        this.dataSource.sort = this.sort;
    }

    updateDatasetCounter(count: number) {
        this.dataSource.datasetIndex = count - 1;
    }

    updateRecordCounter(count: number) {
        this.dataSource.recordsIndex = count - 1;
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
        this.inputOutputToggled$.next(true);
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
                case ValueJsonClass.BinaryValue:
                    return valueObject.value;
                default: {
                    return "Unknown Type";
                }
            }
        }
    }
}
