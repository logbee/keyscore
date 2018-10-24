import {MatTableDataSource} from "@angular/material";
import {BehaviorSubject, Observable} from "rxjs/index";
import {DatasetTableModel, DatasetTableRowModel, DatasetTableRowModelData} from "../models/dataset/DatasetTableModel";
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

export class DatasetDataSource extends MatTableDataSource<DatasetTableRowModel> {
    constructor(datasets$: Observable<DatasetTableModel[]>, index$: Observable<number>, recordsIndex$: Observable<number>) {
        super();
        combineLatest(datasets$, index$, recordsIndex$).subscribe(([datasets, index, recordsIndex]) => {
            this.data = datasets[index].records[recordsIndex].rows;
        });

        this.filterPredicate = (datasetModel: DatasetTableRowModel, filter: string) => {
            let searchString = filter.trim();
            return this.filterAccessingRules(datasetModel, searchString);
        };

        this.sortingDataAccessor = (datasetModel: DatasetTableRowModel, property: string) => {
            switch (property) {
                case "fields":
                    return datasetModel.input.name;
                case "jsonClass":
                    return datasetModel.input.value.jsonClass;
                case "inValues":
                    return this.accessFieldValues(datasetModel.input.value);
                case "outValues":
                    return this.accessFieldValues(datasetModel.input.value);
            }
        }
    }

    connect(): BehaviorSubject<DatasetTableRowModel[]> {
        return super.connect()
    }

    disconnect() {

    }

    private filterAccessingRules(datasetModel: DatasetTableRowModel, searchString) {
        console.log("searchstring is: ", searchString + "name is: " + datasetModel.input.name);
        return datasetModel.input.name.includes(searchString) || datasetModel.input.name.includes(searchString.toLowerCase()) ||
            datasetModel.input.value.jsonClass.toLowerCase().includes(searchString.toLowerCase()) ||
            this.accessFieldValues(datasetModel.input.value).includes(searchString) ||
            this.accessFieldValues((datasetModel.input.value)).includes(searchString.toLowerCase()) ||
            datasetModel.output.name.includes(searchString) || datasetModel.output.name.includes(searchString.toLowerCase()) ||
            datasetModel.output.value.jsonClass.toLowerCase().includes(searchString.toLowerCase()) ||
            this.accessFieldValues(datasetModel.output.value).includes(searchString) || this.accessFieldValues(datasetModel.output.value).includes(searchString.toLowerCase()) ||
            this.accessFieldValues(datasetModel.output.value).toUpperCase().includes(searchString);
    }

    accessFieldValues(valueObject: Value): string {
        switch (valueObject.jsonClass) {
            case ValueJsonClass.BooleanValue: {
                               return (valueObject as BooleanValue).value.toString();
            }
            case ValueJsonClass.TextValue: {
                console.log("returning :", (valueObject as TextValue).value);
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
        }
    }
}