import {Component} from "@angular/core";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {DatasetDataSource} from "../../dataSources/DatasetDataSource";
import {selectExtractedDatasets, selectExtractFinish} from "../live-editing.reducer";
import {select, Store} from "@ngrx/store";
import {BehaviorSubject} from "rxjs/index";
import {filter, take} from "rxjs/internal/operators";

@Component({
    selector: "dataset-table",
    animations: [
        trigger('detailExpand', [
            state('collapsed', style({height: '0px', minHeight: '0', visibility: 'hidden'})),
            state('expanded', style({height: '*', visibility: 'visible'})),
            transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
        ]),
    ],
    template: `
        <table fxFlex="95" #table mat-table [dataSource]="dataSource"
               class="mat-elevation-z8 table-position">
            <ng-container matColumnDef="fields">
                <th mat-header-cell *matHeaderCellDef>Fields</th>
                <td mat-cell *matCellDef="let record">{{record.field.name}}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="['fields']"></tr>
            <tr mat-row *matRowDef="let row; columns: ['fields']"></tr>
        </table>
    `
})

export class DatasetTable {
    private datasets$ = this.store.pipe(select(selectExtractedDatasets));
    private index: BehaviorSubject<number> = new BehaviorSubject<number>(0);
    private dataSource: DatasetDataSource;
    constructor(private store: Store<any>) {
        this.store.pipe(select(selectExtractFinish), filter(extractFinish => extractFinish), take(1)).subscribe(_ =>
            this.dataSource = new DatasetDataSource(this.datasets$, this.index.asObservable()));
    }



}