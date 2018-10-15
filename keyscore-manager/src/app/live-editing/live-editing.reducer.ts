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
import {Value, ValueJsonClass} from "../models/dataset/Value";
import {Record} from "../models/dataset/Record";


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
        records: [{fields: [{name: "test", value: {jsonClass: ValueJsonClass.TextValue, value: "test"}}]}]
    }
};

function createDatasetTableModel(inputDataset: Dataset, outputDataset: Dataset): DatasetTableModel {

    let zippedRecords = inputDataset.records.map(function (x, y) {
        return [x, outputDataset.records.reverse()[y]]
    });
    console.log("zippedgRecords: " + JSON.stringify(zippedRecords));

    const datasetTableRecordModels = zippedRecords.map(([inRecord, outRecord]) => {
        const fieldNames: string[] = [].concat(...inRecord.fields, outRecord.fields).map(field => field.name);
        let fieldNameSet: string[] =  Array.from(new Set(fieldNames));
        fieldNameSet = fieldNameSet.filter(fieldName => fieldName !== "test");

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

function createDatasetTableRowModelData(input: Field, output: Field): DatasetTableRowModel {
    let inputDataModel: DatasetTableRowModelData;
    let outputDataModel: DatasetTableRowModelData;

    if (input === undefined) {
        inputDataModel = new DatasetTableRowModelData(output.name, ValueJsonClass.TextValue, {jsonClass: ValueJsonClass.TextValue, value: "No output yet"}, ChangeType.Added);
    } else {
        inputDataModel = new DatasetTableRowModelData(input.name, input.value.jsonClass, input.value, ChangeType.Unchanged);
    }

    if (output === undefined) {
        outputDataModel = new DatasetTableRowModelData(input.name, ValueJsonClass.TextValue, {jsonClass: ValueJsonClass.TextValue, value: "No output yet"}, ChangeType.Deleted);
    } else {
        outputDataModel = new DatasetTableRowModelData(output.name, output.value.jsonClass, output.value, ChangeType.Unchanged);
    }

    return new DatasetTableRowModel(inputDataModel, outputDataModel)
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
            let resultModels: DatasetTableModel[] = [];
            let zipped = result.datasetsRaw.map(function (x, y) {
                return [x, action.datasets.reverse()[y]]
            });
            zipped.map(([input, output]) => {
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

export const extractFinish = (state: FilterState) => state.extractFinish;


export const resultAvailable = (state: FilterState) => state.resultAvailable;


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


