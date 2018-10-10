import {MatTableDataSource} from "@angular/material";
import {Dataset} from "../models/dataset/Dataset";
import {BehaviorSubject, Observable} from "rxjs/index";
import {DatasetTableModel} from "../models/dataset/DatasetTableModel";
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
import {combineLatest} from "rxjs";

export class DatasetDataSource extends MatTableDataSource<DatasetTableModel> {
    constructor(datasets$: Observable<Dataset[]>, index$: Observable<number>) {
        super();
        combineLatest(datasets$, index$).subscribe(([datasets, index]) => {
            const rows = [];
            console.log("Index observable changed:" + index);
            datasets[index].records[0].fields.forEach(field =>
                rows.push({metadata: datasets[index].metaData, field: field}));
            this.data = rows;
        });
        this.filterPredicate = (datasetModel: DatasetTableModel, filter: string) => {
            let searchString = filter.trim().toLowerCase();
            return this.filterAccessingRules(datasetModel, searchString);
        };
        this.sortingDataAccessor = (datasetModel: DatasetTableModel, property: string) => {
            switch (property) {
                case "fields":
                    return datasetModel.field.name;
                case "jsonClass":
                    return datasetModel.field.value.jsonClass;
                case "values":
                    return this.accessFieldValues(datasetModel.field.value);
            }
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