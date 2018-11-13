import {
    DRAIN_FILTER_SUCCESS,
    EXTRACT_OUTPUT_DATASETS_SUCESS,
    EXTRACT_DATASETS_RESULT_SUCCESS,
    LiveEditingActions,
    LOAD_FILTER_BLUEPRINT_SUCCESS,
    LOAD_FILTER_CONFIGURATION_SUCCESS,
    LOAD_FILTERSTATE_SUCCESS,
    PAUSE_FILTER_SUCCESS,
    RESET_ACTION,
    RESOLVED_DESCRIPTOR_FOR_BLUEPRINT,
    SAVE_UPDATED_CONFIGURATION,
    EXTRACT_INPUT_DATASETS_SUCESS,
    INITIAL_EXTRACT_SUCCESS,
    UpdateConfigurationInBackend,
    UPDATE_CONFIGURATION_IN_BACKEND
} from "./live-editing.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {Configuration} from "../models/common/Configuration";
import {ResourceInstanceState} from "../models/filter-model/ResourceInstanceState";
import {ResourceStatus} from "../models/filter-model/ResourceStatus";
import {Blueprint} from "../models/blueprints/Blueprint";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {
    ChangeType,
    DatasetTableModel,
    DatasetTableRecordModel,
    DatasetTableRowModel,
    DatasetTableRowModelData
} from "../models/dataset/DatasetTableModel";
import {Dataset} from "../models/dataset/Dataset";
import {Field} from "../models/dataset/Field";
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
import {Record} from "../models/dataset/Record";
import {v4 as uuid} from "uuid"
import {Label} from "../models/common/MetaData";


export class FilterState {
    public initialConfiguration: Configuration;
    public updatedConfiguration: Configuration;
    public blueprint: Blueprint;
    public descriptor: ResolvedFilterDescriptor;
    public filterState: ResourceInstanceState;
    public datasetsModels: DatasetTableModel[];
    public inputDatasets: Dataset[];
    public outputDatasetsRaw: Dataset[];
    public extractFinish: boolean;
    public isUpdated: boolean;
    public resultAvailable: boolean;
    public currentDatasetCounter: number;
    public dummyDataset: Dataset;

}

const initialState: FilterState = {
    initialConfiguration: null,
    updatedConfiguration: null,
    blueprint: null,
    descriptor: null,
    filterState: {
        id: "",
        health: null,
        throughPutTime: 0,
        totalThroughputTime: 0,
        status: ResourceStatus.Unknown
    },
    extractFinish: false,
    isUpdated: false,
    resultAvailable: false,
    datasetsModels: [],
    inputDatasets: [],
    outputDatasetsRaw: [],
    currentDatasetCounter: 0,
    dummyDataset: {
        metaData: {labels: []},
        records: [{fields: [{name: "dummy", value: {jsonClass: ValueJsonClass.TextValue, value: "dummy"}}]}]
    }
};

export function LiveEditingReducer(state: FilterState = initialState, action: LiveEditingActions): FilterState {

    const result: FilterState = Object.assign({}, state);


    switch (action.type) {
        case LOAD_FILTER_CONFIGURATION_SUCCESS:
            result.isUpdated = false;
            result.initialConfiguration = action.configuration;
            break;
        case LOAD_FILTERSTATE_SUCCESS:
            result.filterState = action.state;
            break;
        case DRAIN_FILTER_SUCCESS:
            result.extractFinish = false;
            result.filterState = action.state;
            break;
        case PAUSE_FILTER_SUCCESS:
            result.filterState = action.state;
            break;
        case RESOLVED_DESCRIPTOR_FOR_BLUEPRINT:
            result.descriptor = action.descriptor;
            break;
        case INITIAL_EXTRACT_SUCCESS:
            let initialResultModel: DatasetTableModel[] = [];
            if (!action.input.map(elem => elem.metaData === undefined).length) {
                action.input.map(datset => {
                    datset.metaData.labels.push(
                        {
                            name: "io.logbee.keyscore.manager.live-editing.id",
                            value: {jsonClass: ValueJsonClass.TextValue, value: uuid()}
                        }
                    )
                });
            }
            result.inputDatasets = action.input;
            result.datasetsModels = [];
            const models = [];
            action.input.forEach(dataset => {
                let model = createDatasetTableModel(dataset, state.dummyDataset, state.resultAvailable);
                models.push(model)
            });
            let initialZippedDatasets = computeZippedDatasets(result.inputDatasets, action.output);
            result.extractFinish = true;
            result.datasetsModels = models;
            result.resultAvailable = true;
            initialZippedDatasets.map(([input, output]) => {
                initialResultModel.push(createDatasetTableModel(input, output, result.resultAvailable));
            });
            result.datasetsModels = initialResultModel;
            result.extractFinish = true;
            break;
        case SAVE_UPDATED_CONFIGURATION:
            result.updatedConfiguration = action.configuration;
            break;
        case EXTRACT_DATASETS_RESULT_SUCCESS:
            let resultModels: DatasetTableModel[] = [];
            let zippedDatasets = computeZippedDatasets(result.inputDatasets, action.datasets.reverse());
            result.resultAvailable = true;
            zippedDatasets.map(([input, output]) => {
                resultModels.push(createDatasetTableModel(input, output, result.resultAvailable));
            });
            result.datasetsModels = resultModels;
            result.extractFinish = true;
            break;
        case RESET_ACTION:
            return Object.assign({}, initialState);
        case LOAD_FILTER_BLUEPRINT_SUCCESS:
            result.resultAvailable = false;
            result.blueprint = action.blueprint;
            break;
    }
    return result;
}

function findMatch(dataset: Dataset, datasets: Dataset[]) {
    let found: Label = dataset.metaData.labels.find(label => label.name === 'io.logbee.keyscore.manager.live-editing.id');
    let inputDatasetId = (found.value as TextValue).value;
    let resultDataset: Dataset;
    datasets.map(dataset => {
        let datasetId = (dataset.metaData.labels.find(label => label.name === 'io.logbee.keyscore.manager.live-editing.id').value as TextValue).value;
        if (datasetId === inputDatasetId) {
            resultDataset = dataset;
        }
    });
    return resultDataset;
}

export const extractFinish = (state: FilterState) => state.extractFinish;

export const getFilterState = createFeatureSelector<FilterState>(
    "filter"
);
export const selectInitialConfiguration = createSelector(getFilterState, (state: FilterState) => state.initialConfiguration);

export const selectUpdatedConfiguration = createSelector(getFilterState, (state: FilterState) => state.updatedConfiguration);

export const selectLiveEditingFilterState = createSelector(getFilterState, (state: FilterState) => state.filterState);

export const selectDatasetsModels = createSelector(getFilterState, (state: FilterState) => state.datasetsModels);

export const selectDatasetsRaw = createSelector(getFilterState, (state: FilterState) => state.inputDatasets);

export const selectExtractFinish = createSelector(getFilterState, extractFinish);

export const selectCurrentDescriptor = createSelector(getFilterState, (state: FilterState) => state.descriptor);

export const selectCurrentBlueprint = createSelector(getFilterState, (state: FilterState) => state.blueprint);

export const selectResultAvailable = createSelector(getFilterState, (state: FilterState) => state.resultAvailable);



//Additional functions for DatatableModels

function createDatasetTableModel(inputDataset: Dataset, outputDataset: Dataset, resultAvailable: boolean): DatasetTableModel {

    let zippedRecords = inputDataset.records.map(function (x, y) {
        return [x, outputDataset.records[y]]
    });

    const datasetTableRecordModels = zippedRecords.map(([inRecord, outRecord]) => {
        const fieldNames: string[] = [].concat(...inRecord.fields, outRecord.fields).map(field => field.name);
        let fieldNameSet: string[] = Array.from(new Set(fieldNames));
        fieldNameSet = fieldNameSet.filter(fieldName => fieldName !== "dummy");
        const datasetTableRowModels = Array.from(fieldNameSet).map(name => {
            return createDatasetTableRowModelData(findFieldByName(name, inRecord), findFieldByName(name, outRecord), resultAvailable);
        });

        return new DatasetTableRecordModel(datasetTableRowModels)
    });

    return new DatasetTableModel(inputDataset.metaData, outputDataset.metaData, datasetTableRecordModels);
}

function findFieldByName(name: string, record: Record): Field {
    return record.fields.find(field => field.name === name);
}

function checkForValueChange(input: Value, output: Value) {
    return accessFieldValues(input) !== accessFieldValues(output);
}

function accessFieldValues(valueObject: Value): any {
    if (!valueObject) {
        return ""
    } else {
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
                return (valueObject as DurationValue).seconds.toString();

            }
            case ValueJsonClass.TimestampValue: {
                return (valueObject as TimestampValue).seconds.toString();
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

function createDatasetTableRowModelData(input: Field, output: Field, resultAvailable: boolean): DatasetTableRowModel {
    let inputDataModel: DatasetTableRowModelData;
    let outputDataModel: DatasetTableRowModelData;
    if (input === undefined && output != undefined ) {
        inputDataModel = new DatasetTableRowModelData(output.name, ValueJsonClass.TextValue, {
            jsonClass: ValueJsonClass.TextValue,
            value: "Field was added!"
        }, ChangeType.Added);
        outputDataModel = new DatasetTableRowModelData(output.name, output.value.jsonClass, output.value, ChangeType.Unchanged);

    }
    if (input != undefined && output === undefined) {
        if (resultAvailable) {
            outputDataModel = new DatasetTableRowModelData(input.name, ValueJsonClass.TextValue, {
                jsonClass: ValueJsonClass.TextValue,
                value: "Field was deleted!"
            }, ChangeType.Deleted);
        } else {
            outputDataModel = new DatasetTableRowModelData(input.name, ValueJsonClass.TextValue, {
                jsonClass: ValueJsonClass.TextValue,
                value: "Field was deleted!"
            }, ChangeType.Unchanged);
        }
        inputDataModel = new DatasetTableRowModelData(input.name, input.value.jsonClass, input.value, ChangeType.Unchanged);
    } else if (input != undefined && output != undefined) {
        if (checkForValueChange(input.value, output.value)) {
            inputDataModel = new DatasetTableRowModelData(input.name, input.value.jsonClass, input.value, ChangeType.Modified);
            outputDataModel = new DatasetTableRowModelData(output.name, output.value.jsonClass, output.value, ChangeType.Modified);
        } else {
            inputDataModel = new DatasetTableRowModelData(input.name, input.value.jsonClass, input.value, ChangeType.Unchanged);
            outputDataModel = new DatasetTableRowModelData(output.name, output.value.jsonClass, output.value, ChangeType.Unchanged);
        }
    }

    return new DatasetTableRowModel(inputDataModel, outputDataModel);
}

function computeZippedDatasets(inputDatasets:Dataset[], extractedDatasets: Dataset[]): Dataset[][] {
    let zippedDatasets: Dataset[][] = [];
    if (inputDatasets.filter(dataset => dataset.metaData === undefined)) {
        // FallBack on order
        zippedDatasets = inputDatasets.map((x, y) => {
            return [x, extractedDatasets[y]]
        });
    } else {
        // MapRecords by Id
        zippedDatasets = inputDatasets.map(dataset => {
            return ([dataset, findMatch(dataset, extractedDatasets)]);
        });
    }
    return zippedDatasets;
}