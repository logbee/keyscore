import {MatTableDataSource} from "@angular/material";
import {Dataset} from "../models/dataset/Dataset";
import {BehaviorSubject, Observable} from "rxjs/index";
import {DatasetTableModel} from "../models/dataset/DatasetTableModel";
import {withLatestFrom} from "rxjs/internal/operators";

export class DatasetDataSource extends MatTableDataSource<DatasetTableModel> {
    constructor(datasets$: Observable<Dataset[]>, index$: Observable<number>) {
        super();
        datasets$.pipe(withLatestFrom(index$)).subscribe(([datasets,index]) => {
            const rows = [];
            datasets[index].records[0].fields.forEach(field =>
                rows.push({metadata: datasets[index].metaData, field: field}, {metadata: datasets[index].metaData, field: field} ));
            this.data = rows;
        });

    }
    connect(): BehaviorSubject<DatasetTableModel[]> {
        return super.connect()
    }

    disconnect() {

    }
}