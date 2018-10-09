import {MatTableDataSource} from "@angular/material";
import {Dataset} from "../models/dataset/Dataset";
import {BehaviorSubject, Observable} from "rxjs/index";
import {DatasetTableModel} from "../models/dataset/DatasetTableModel";
import {withLatestFrom} from "rxjs/internal/operators";
import {
    BooleanValue,
    DecimalValue,
    DurationValue,
    NumberValue,
    TextValue,
    TimestampValue,
    Value,
    ValueJsonClass
} from "../models/dataset/Value";

export class DatasetDataSource extends MatTableDataSource<DatasetTableModel> {
    constructor(datasets$: Observable<Dataset[]>, index$: Observable<number>) {
        super();
        datasets$.pipe(withLatestFrom(index$)).subscribe(([datasets, index]) => {
            const rows = [];
            datasets[index].records[0].fields.forEach(field =>
                rows.push({metadata: datasets[index].metaData, field: field}));
            this.data = rows;
        });
        this.filterPredicate = (datasetModel: DatasetTableModel, filter: string) => {
            let searchString = filter.trim().toLowerCase();
            return this.filterAccessingRules(datasetModel, searchString);
        }
    }
    connect(): BehaviorSubject<DatasetTableModel[]> {
        return super.connect()
    }
    disconnect() {

    }

    private filterAccessingRules(datasetModel: DatasetTableModel, searchString) {
        return datasetModel.field.name.includes(searchString) ||
            datasetModel.field.value.jsonClass.toLowerCase().includes(searchString) ||
            this.accessFieldValues(datasetModel.field.value).includes(searchString);
    }

    accessFieldValues(valueObject: Value): string {
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
                return (valueObject as DurationValue).nanos + (valueObject as DurationValue).seconds;
            }
            case ValueJsonClass.TimestampValue: {
                return (valueObject as TimestampValue).nanos + (valueObject as TimestampValue).seconds;
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