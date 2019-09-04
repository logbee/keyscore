import {MatTableDataSource} from "@angular/material";
import {BehaviorSubject} from "rxjs/index";
import {
    DatasetTableModel,
    DatasetTableRowModel,
    ValueJsonClass,
    Value

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
    private accessFieldValues(valueObject: Value): any {
        if (!valueObject) {
            return "No extracted datasets yet!"
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

    private checkFilterMatch(model, searchString) {
        return model.name.includes(searchString) || model.value.jsonClass.includes(searchString) || this.accessFieldValues(model.value).includes(searchString);
    }
}

