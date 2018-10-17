import {
    DRAIN_FILTER_SUCCESS,
    EXTRACT_DATASETS_INITIAL_SUCCESS,
    EXTRACT_DATASETS_RESULT_SUCCESS,
    LiveEditingActions,
    LOAD_FILTER_BLUEPRINT_SUCCESS,
    LOAD_FILTER_CONFIGURATION_SUCCESS,
    LOAD_FILTERSTATE_SUCCESS,
    PAUSE_FILTER_SUCCESS,
    RESOLVED_DESCRIPTOR_FOR_BLUEPRINT,
    SAVE_UPDATED_CONFIGURATION
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
    DecimalValue, DurationValue,
    NumberValue,
    TextValue,
    TimestampValue,
    Value,
    ValueJsonClass
} from "../models/dataset/Value";
import {Record} from "../models/dataset/Record";
import {v4 as uuid} from "uuid"
import {Label} from "../models/common/MetaData";
import {state} from "@angular/animations";
import {isNullOrUndefined} from "util";


export class FilterState {
    public initialConfiguration: Configuration;
    public updatedConfiguration: Configuration;
    public blueprint: Blueprint;
    public descriptor: ResolvedFilterDescriptor;
    public filterState: ResourceInstanceState;
    public datasetsModels: DatasetTableModel[];
    public datasetsRaw: Dataset[];
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
    datasetsRaw: [],
    currentDatasetCounter: 0,
    dummyDataset: {
        metaData: {labels: []},
        records: [{fields: [{name: "dummy", value: {jsonClass: ValueJsonClass.TextValue, value: "dummy"}}]}]
    }
};

function createDatasetTableModel(inputDataset: Dataset, outputDataset: Dataset): DatasetTableModel {

    let zippedRecords = inputDataset.records.map(function (x, y) {
        return [x, outputDataset.records[y]]
    });

    zippedRecords.map(([inRecord, outRecord]) => {
        // console.log("InRecord Fields " + inRecord.fields.map(field => field.name));
        // console.log("OutRecord Fields " + outRecord.fields.map(field => field.name));
    });

    const datasetTableRecordModels = zippedRecords.map(([inRecord, outRecord]) => {
        const fieldNames: string[] = [].concat(...inRecord.fields, outRecord.fields).map(field => field.name);
        let fieldNameSet: string[] = Array.from(new Set(fieldNames));
        fieldNameSet = fieldNameSet.filter(fieldName => fieldName !== "dummy");
        // console.log(fieldNameSet);
        const datasetTableRowModels = Array.from(fieldNameSet).map(name => {
            return createDatasetTableRowModelData(findFieldByName(name, inRecord), findFieldByName(name, outRecord));
        });

        return new DatasetTableRecordModel(datasetTableRowModels)
    });

    return new DatasetTableModel(inputDataset.metaData, outputDataset.metaData, datasetTableRecordModels);
}

function findFieldByName(name: string, record: Record): Field {
    return record.fields.find(field => field.name === name);
}

function checkForValueChange(input: Value, output: Value) {
    // console.log(accessFieldValues(input) + "in/out" + accessFieldValues(output));
    return accessFieldValues(input) !== accessFieldValues(output);
}

function  accessFieldValues(valueObject: Value): any {
    if (!valueObject) {
        return "No output yet!"
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

function createDatasetTableRowModelData(input: Field, output: Field): DatasetTableRowModel {
    let inputDataModel: DatasetTableRowModelData;
    let outputDataModel: DatasetTableRowModelData;
    let guard = ([input, output]);

    // switch (guard) {
    //     case [undefined, output]:
    //         console.log("matched (undefined, output)");
    //         inputDataModel = new DatasetTableRowModelData(output.name, ValueJsonClass.TextValue, {
    //                     jsonClass: ValueJsonClass.TextValue,
    //                     value: "No output yet"
    //                 }, ChangeType.Added);
    //         outputDataModel = new DatasetTableRowModelData(output.name, output.value.jsonClass, output.value, ChangeType.Unchanged);
    //         break;
    //     case [input, undefined]:
    //         console.log("matched (input, undefined)");
    //         outputDataModel = new DatasetTableRowModelData(input.name, ValueJsonClass.TextValue, {
    //                     jsonClass: ValueJsonClass.TextValue,
    //                     value: "Field was deleted"
    //                 }, ChangeType.Deleted);
    //         inputDataModel = new DatasetTableRowModelData(input.name, input.value.jsonClass, input.value, ChangeType.Unchanged);
    //         break;
    //     case [input, output]:
    //         console.log("matched (input, output)");
    //         checkForValueChange(input.value, output.value);
            // inputDataModel = new DatasetTableRowModelData(input.name, input.value.jsonClass, input.value, ChangeType.Unchanged);
            // outputDataModel = new DatasetTableRowModelData(output.name, output.value.jsonClass, output.value, ChangeType.Unchanged);
            // break;
    // }

    if (input === undefined && output != undefined) {
        inputDataModel = new DatasetTableRowModelData(output.name, ValueJsonClass.TextValue, {
            jsonClass: ValueJsonClass.TextValue,
            value: "No output yet"
        }, ChangeType.Added);
        outputDataModel = new DatasetTableRowModelData(output.name, output.value.jsonClass, output.value, ChangeType.Unchanged);

    } else if(input != undefined && output === undefined) {
        outputDataModel = new DatasetTableRowModelData(input.name, ValueJsonClass.TextValue, {
            jsonClass: ValueJsonClass.TextValue,
            value: "Field was deleted"
        }, ChangeType.Deleted);
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
        case EXTRACT_DATASETS_INITIAL_SUCCESS:
            if (action.datasets.map(elem => elem.metaData === undefined)) {
                // console.log("No Metadata available fallback on order of datasets");
            } else {
                action.datasets.map(datset => {
                    datset.metaData.labels.push(
                        {
                            name: "io.logbee.keyscore.manager.live-editing.id",
                            value: {jsonClass: ValueJsonClass.TextValue, value: uuid()}
                        }
                    )
                });
            }
            result.datasetsRaw = action.datasets;
            result.datasetsModels = [];
            const models = [];
            action.datasets.forEach(dataset => {
                let model = createDatasetTableModel(dataset, state.dummyDataset);
                models.push(model)
            });
            result.extractFinish = true;
            result.datasetsModels = models;
            break;
        case SAVE_UPDATED_CONFIGURATION:
            result.updatedConfiguration = action.configuration;
            break;
        case EXTRACT_DATASETS_RESULT_SUCCESS:
            let extractedDatasets: Dataset[] = action.datasets.reverse();
            let resultModels: DatasetTableModel[] = [];
            let rawDatasets = result.datasetsRaw;
            let zippedDatasets = [];
            if (rawDatasets.filter(datast => datast.metaData === undefined)) {
                // FallBack on order
                zippedDatasets = result.datasetsRaw.map(function (x, y) {
                    return [x, extractedDatasets[y]]
                });
            } else {
                // MapRecords by Id
                zippedDatasets = result.datasetsRaw.map(dataset => {
                    return ([dataset, findMatch(dataset, extractedDatasets)]);
                })
            }
            zippedDatasets.map(([input, output]) => {
                resultModels.push(createDatasetTableModel(input, output));
            });
            result.datasetsModels = resultModels;
            result.resultAvailable = true;
            result.extractFinish = true;
            break;
        case LOAD_FILTER_BLUEPRINT_SUCCESS:
            result.blueprint = action.blueprint;
            break;
    }
    return result;
}

function findMatch(dataset: Dataset, datasets: Dataset[]) {
    let found: Label = dataset.metaData.labels.find(label => label.name === 'io.logbee.keyscore.manager.live-editing.id');
    let inputDatasetId = (found.value as TextValue).value;

    datasets.map(dataset => {
        let datasetId = (dataset.metaData.labels.find(label => label.name === 'io.logbee.keyscore.manager.live-editing.id').value as TextValue).value;
        if (datasetId === inputDatasetId) {
            return dataset;
        }
    });
}

export const extractFinish = (state: FilterState) => state.extractFinish;

export const getFilterState = createFeatureSelector<FilterState>(
    "filter"
);
export const selectConfiguration = createSelector(getFilterState, (state: FilterState) => state.initialConfiguration);

export const selectLiveEditingFilterState = createSelector(getFilterState, (state: FilterState) => state.filterState);

export const selectDatasetsModels = createSelector(getFilterState, (state: FilterState) => state.datasetsModels);

export const selectDatasetsRaw = createSelector(getFilterState, (state: FilterState) => state.datasetsRaw);

export const selectExtractFinish = createSelector(getFilterState, extractFinish);

export const selectCurrentDescriptor = createSelector(getFilterState, (state: FilterState) => state.descriptor);

export const selectCurrentBlueprint = createSelector(getFilterState, (state: FilterState) => state.blueprint);

export const selectUpdatedConfiguration = createSelector(getFilterState, (state: FilterState) => state.updatedConfiguration);

export const selectResultAvailable = createSelector(getFilterState, (state: FilterState) => state.resultAvailable);
