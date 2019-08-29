import {MatTableDataSource} from "@angular/material";
import {BehaviorSubject} from "rxjs/index";
import {
    DatasetTableModel,
    DatasetTableRowModel,
    BooleanValue,
    DecimalValue,
    DurationValue,
    NumberValue,
    TextValue,
    TimestampValue,
    Value,
    ValueJsonClass
} from "@keyscore-manager-models";

export class DatasetDataSource extends MatTableDataSource<DatasetTableRowModel> {
    readonly numberOfDataset: number = 0;
    readonly numberOfRecords: number = 0;

    constructor(datasetTableModels: Map<string, DatasetTableModel[]>, index: number, recordsIndex: number, selectedBlock: string) {
        super();
        console.log(selectedBlock);
        let model = datasetTableModels.get(selectedBlock);
        console.log(model);
        if (model && model[index]) {
            this.numberOfDataset = model.length;
            this.numberOfRecords = model[index].records.length;
            this.data = model[index].records[recordsIndex].rows;
        }
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

    public getNumberOfDatsets() {
        return this.numberOfDataset;
    }

    public getNumberOfRecords() {
        return this.numberOfRecords;
    }

    private filterAccessingRules(datasetModel: DatasetTableRowModel, searchString) {
        let input = datasetModel.input;
        let output = datasetModel.output;
        return this.checkFilterMatch(input, searchString.toUpperCase()) ||
            this.checkFilterMatch(input, searchString.toLowerCase()) ||
            this.checkFilterMatch(output, searchString.toUpperCase() ||
            this.checkFilterMatch(output, searchString.toLowerCase()))
    }
    private accessFieldValues(valueObject: Value): string {
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
        }
    }

    private checkFilterMatch(model, searchString) {
        return model.name.includes(searchString) || model.value.jsonClass.includes(searchString) || this.accessFieldValues(model.value).includes(searchString);
    }
}

