import {MatTableDataSource} from "@angular/material";
import {BehaviorSubject} from "rxjs/index";
import {DatasetTableRowModel, DatasetTableModel, DatasetTableRecordModel, DatasetTableRowModelData, ChangeType} from "@keyscore-manager-models/src/main/dataset/DatasetTableModel";
import {Value, ValueJsonClass} from "@keyscore-manager-models/src/main/dataset/Value";
import { Dataset } from "@keyscore-manager-models/src/main/dataset/Dataset";
import { Field } from "@keyscore-manager-models/src/main/dataset/Field";
import { Record } from "@keyscore-manager-models/src/main/dataset/Record";

export class DatasetDataSource extends MatTableDataSource<DatasetTableRowModel> {
    readonly numberOfDataset: number = 0;
    readonly numberOfRecords: number = 0;
    dummyDataset: Dataset = {
        metaData: {labels: []},
        records: [{fields: [{name: "dummy", value: {jsonClass: ValueJsonClass.TextValue, value: "dummy"}}]}]
    };
    results: DatasetTableModel[] = [];


    constructor(datasets: Map<string, Dataset[]>, index: number, recordsIndex: number, selectedBlock: string, where: string) {
        super();
        if (datasets.get(selectedBlock) != undefined) {
            datasets.get(selectedBlock).forEach(dataset => {
                let model: DatasetTableModel = null;
                model = this.createDatasetTableModel(dataset, this.dummyDataset);
                this.results.push(model)
            });
        }

        if (this.results && this.results[index]) {
            this.numberOfDataset = this.results.length;
            this.numberOfRecords = this.results[index].records.length;
            this.data = this.results[index].records[recordsIndex].rows;
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
    private createDatasetTableModel(inputDataset: Dataset, outputDataset: Dataset): DatasetTableModel {
        const recordModels = inputDataset.records.map(record => {
            const fieldnames = record.fields.map(field => field.name);
            const datsetTableRowModels = fieldnames.map(name => {
                return this.createDatasetTableRowModel(this.findFieldByName(name, record));
            });
            return new DatasetTableRecordModel(datsetTableRowModels)
        });

        return new DatasetTableModel(inputDataset.metaData, outputDataset.metaData, recordModels)
    }

    private createDatasetTableRowModel(field: Field): DatasetTableRowModel {
        let datamodel: DatasetTableRowModelData;

        datamodel = new DatasetTableRowModelData(field.name, field.value.jsonClass, field.value, ChangeType.Unchanged);

        return new DatasetTableRowModel(datamodel, datamodel);
    }

//TODO: Breaks when there are two fields with the same name !
    private findFieldByName(name: string, record: Record): Field {
        return record.fields.find(field => field.name === name);
    }
}

