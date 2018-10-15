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
import {DatasetTableModel, DatasetTableRowModel, DatasetTableRowModelData} from "../models/dataset/DatasetTableModel";
import {Dataset} from "../models/dataset/Dataset";
import {Field} from "../models/dataset/Field";

export class FilterState {
    public initialConfiguration: Configuration;
    public updatedConfiguration: Configuration;
    public blueprint: Blueprint;
    public descriptor: ResolvedFilterDescriptor;
    public filterState: ResourceInstanceState;
    public datasets: DatasetTableModel[];
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
    datasets: [],
    currentDatasetCounter: 0,
    dummyDataset: {
        metaData: {labels: []},
        records: [{fields: [{name: "test", value: undefined}]}]
    }
};

function createDatasetTableModel(inputDataset: Dataset, outputDataset: Dataset): DatasetTableModel {
    let rows: DatasetTableRowModel[] = [];
    let model = new DatasetTableModel(inputDataset.metaData, outputDataset.metaData, rows);

    inputDataset.records[0].fields.forEach(field => {
        rows.push(new DatasetTableRowModel(createDatasetTableRowModelData(field), undefined));
    });
    model.rows = rows;
    return model;
}

function createDatasetTableRowModelData(field: Field): DatasetTableRowModelData {
    return  new DatasetTableRowModelData(field.name, field.value.jsonClass,field.value);
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
            result.datasets = [];
                const models = [];
                action.datasets.forEach(dataset => {
                let model = createDatasetTableModel(dataset, state.dummyDataset);
                models.push(model)
            });
            result.extractFinish = true;
            result.datasets = models;
            break;
        case SAVE_UPDATED_CONFIGURATION:
            result.updatedConfiguration = action.configuration;
            break;
        case EXTRACT_DATASETS_RESULT_SUCCESS:
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

export const selectDatasets = createSelector(getFilterState, (state: FilterState) => state.datasets);

export const selectExtractFinish = createSelector(getFilterState, extractFinish);

export const selectCurrentDescriptor = createSelector(getFilterState, (state: FilterState) => state.descriptor);

export const selectCurrentBlueprint = createSelector(getFilterState, (state: FilterState) => state.blueprint);

export const selectUpdatedConfiguration = createSelector(getFilterState, (state: FilterState) => state.updatedConfiguration);


