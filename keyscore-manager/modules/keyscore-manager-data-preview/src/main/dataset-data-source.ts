import {MatTableDataSource} from "@angular/material";
import {BehaviorSubject} from "rxjs";
import {
    DatasetTableRowModel,
    DatasetTableModel,
    DatasetTableRecordModel,
    DatasetTableRowModelData,
    ChangeType
} from "@keyscore-manager-models/src/main/dataset/DatasetTableModel";
import {Value, ValueJsonClass, MimeType} from "@keyscore-manager-models/src/main/dataset/Value";
import {Dataset} from "@keyscore-manager-models/src/main/dataset/Dataset";
import {Field} from "@keyscore-manager-models/src/main/dataset/Field";
import {Record} from "@keyscore-manager-models/src/main/dataset/Record";

export class DatasetDataSource extends MatTableDataSource<DatasetTableRowModel> {

    dummyDataset: Dataset = {
        metaData: {labels: []},
        records: [{fields: [{name: "dummy", value: {jsonClass: ValueJsonClass.TextValue, value: "dummy", mimetype: MimeType.TEXT_PLAIN}}]}]
    };

    set datasets(val: Dataset[]) {
        if (val) {
            this._datasetTableModels = [];

            val.forEach(dataset => {
                const model = this.createDatasetTableModel(dataset, this.dummyDataset);
                this._datasetTableModels.push(model);
            });
            if (!this.datasetIndex || !(this._datasetTableModels && this._datasetTableModels[this.datasetIndex])) {
                this.datasetIndex = 0;
            } else {
                this.datasetIndex = this.datasetIndex;
            }
        }
    }

    set datasetIndex(val: number) {
        this._datasetIndex = val;
        console.log(`Set dataset index: recordsIndex = ${this.recordsIndex} | datasetIndex = ${this.datasetIndex} | tableModels = ${this._datasetTableModels}`);
        if (!this.recordsIndex || !(this._datasetTableModels && this._datasetTableModels[this.datasetIndex] && this._datasetTableModels[this.datasetIndex].records[this.recordsIndex])) {
            this.recordsIndex = 0;
        } else {
            this.recordsIndex = this.recordsIndex;
        }
    }

    get datasetIndex(): number {
        return this._datasetIndex;
    }

    set recordsIndex(val: number) {
        this._recordsIndex = val;
        this.updateData();
    }

    get recordsIndex(): number {
        return this._recordsIndex;
    }

    private _recordsIndex: number;

    private _datasetIndex: number;

    private _datasetTableModels: DatasetTableModel[] = [];


    get numberOfDatasets(): number {
        if (!this._datasetTableModels) return 0;

        return this._datasetTableModels.length;

    }

    get numberOfRecords(): number {
        if (!(this._datasetTableModels && this._datasetTableModels[this.datasetIndex])) return 0;

        return this._datasetTableModels[this.datasetIndex].records.length;
    }

    constructor(datasets: Dataset[]) {
        super();
        this.datasets = datasets;


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
        return super.connect();
    }

    disconnect() {
        super.disconnect();
    }

    private updateData() {
        if (this._datasetTableModels && this._datasetTableModels[this.datasetIndex]) {
            this.data = this._datasetTableModels[this.datasetIndex].records[this.recordsIndex].rows;
        } else {
            this.data = [];
        }
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
        const datamodel = new DatasetTableRowModelData(field.name, field.value.jsonClass, field.value, ChangeType.Unchanged);

        return new DatasetTableRowModel(datamodel, datamodel);
    }

//TODO: Breaks when there are two fields with the same name !
    private findFieldByName(name: string, record: Record): Field {
        return record.fields.find(field => field.name === name);
    }
}

