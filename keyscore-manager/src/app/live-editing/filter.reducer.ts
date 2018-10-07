import {
    DRAIN_FILTER_SUCCESS, EXTRACT_DATASETS_INITIAL_SUCCESS,
    FiltersActions,
    LOAD_FILTER_BLUEPRINT_SUCCESS,
    LOAD_FILTER_CONFIGURATION_SUCCESS, LOAD_FILTERSTATE_SUCCESS,
    PAUSE_FILTER_SUCCESS, RESOLVED_DESCRIPTOR_FOR_BLUEPRINT, UPDATE_DATASET_COUNTER
} from "./filters.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {Configuration} from "../models/common/Configuration";
import {ResourceInstanceState} from "../models/filter-model/ResourceInstanceState";
import {ResourceStatus} from "../models/filter-model/ResourceStatus";
import {Dataset} from "../models/dataset/Dataset";
import {Blueprint} from "../models/blueprints/Blueprint";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";

export class FilterState {
    public configuration: Configuration;
    public blueprint: Blueprint;
    public descriptor: ResolvedFilterDescriptor;
    public filterState: ResourceInstanceState;
    public exampleDatasets: Dataset[];
    public resultDatasets: Dataset[];
    public extractFinish: boolean;
    public updateConfiguration: boolean;
    public resultAvailable: boolean;
    public currentDatasetCounter: number;

}

const initialState: FilterState = {
    configuration: null,
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
    updateConfiguration: false,
    resultAvailable: false,
    exampleDatasets: [],
    resultDatasets: [],
    currentDatasetCounter: 0,
};

export function FilterReducer(state: FilterState = initialState, action: FiltersActions): FilterState {

    const result: FilterState = Object.assign({}, state);

    switch (action.type) {
        case LOAD_FILTER_CONFIGURATION_SUCCESS:
            result.updateConfiguration = false;
            result.configuration = action.configuration;
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
            result.exampleDatasets = [];
            result.exampleDatasets = action.datasets;
            result.extractFinish = true;
            break;
        // case EXTRACT_DATASETS_RESULT_SUCCESS:
        //     result.resultAvailable = true;
        //     result.resultDatasets = [];
        //     result.resultDatasets = action.datasets.reverse();
        //     result.extractFinish = true;
        //     break;
        // case UPDATE_FILTER_CONFIGURATION:
        //     result.updateConfiguration = true;
        //     result.filter = deepcopy(action.filter);
        //     result.filter.parameters.forEach((p) => {
        //         if (p.jsonClass === "IntParameter") {
        //             p.value = +action.values[p.ref.uuid];
        //         } else {
        //             p.value = action.values[p.ref.uuid];
        //         }
        //     });
        //     break;
        case UPDATE_DATASET_COUNTER:
            result.currentDatasetCounter = action.counter;
            break;
        case LOAD_FILTER_BLUEPRINT_SUCCESS:
            result.blueprint = action.blueprint;
            break;
    }
    return result;
}

export const extractFinish = (state: FilterState) => state.extractFinish;

export const currentDatasetCounter = (state: FilterState) => state.currentDatasetCounter;

export const resultAvailable = (state: FilterState) => state.resultAvailable;

export const getUpdateConfigurationFlag = (state: FilterState) => state.updateConfiguration;

export const getFilterState = createFeatureSelector<FilterState>(
    "filter"
);
export const selectConfiguration = createSelector(getFilterState, (state: FilterState) => state.configuration);

export const selectConfigurationId = createSelector(getFilterState, (state: FilterState) => state.configuration.ref.uuid);

export const selectLiveEditingFilterState = createSelector(getFilterState, (state: FilterState) => state.filterState);

export const selectExtractedDatasets = createSelector(getFilterState, (state: FilterState) => state.exampleDatasets);

export const selectResultDatasets = createSelector(getFilterState, (state: FilterState) => state.resultDatasets);

export const selectExtractFinish = createSelector(getFilterState, extractFinish);

export const selectResultAvailable = createSelector(getFilterState, resultAvailable);

export const selectUpdateConfigurationFlag = createSelector(getFilterState, getUpdateConfigurationFlag);

export const selectCurrentDatasetCounter = createSelector(getFilterState, currentDatasetCounter);

export const selectCurrentDescriptor = createSelector(getFilterState, (state: FilterState) => state.descriptor);

export const selectCurrentBlueprint = createSelector(getFilterState, (state: FilterState) => state.blueprint);

export const selectCurrentBlueprintId = createSelector(getFilterState, (state: FilterState) => state.blueprint.ref.uuid);
