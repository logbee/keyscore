import {
    DRAIN_FILTER_SUCCESS, EXTRACT_DATASETS_INITIAL_SUCCESS, EXTRACT_DATASETS_RESULT_SUCCESS,
    FiltersActions,
    LOAD_FILTERSTATE_SUCCESS,
    LOAD_LIVE_EDITING_FILTER_SUCCESS,
    LOCK_CURRENT_EXAMPLE_DATASET,
    PAUSE_FILTER_SUCCESS, RECONFIGURE_FILTER_SUCCESS, UPDATE_FILTER_CONFIGURATION
} from "./filters.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {FilterConfiguration} from "../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../models/filter-model/FilterInstanceState";
import {FilterStatus} from "../models/filter-model/FilterStatus";
import {Dataset} from "../models/filter-model/dataset/Dataset";
import {deepcopy} from "../util";

export class FilterState {
    public filter: FilterConfiguration;
    public filterState: FilterInstanceState;
    public exampleDatasets: Dataset[];
    public resultDatasets: Dataset[];
    public extractFinish: boolean;
    public updateConfiguration: boolean;
    public resultAvailable: boolean;
    public currentExampleDataset: Dataset;
}

const initialState: FilterState = {
    filter: {
        id: "",
        descriptor: null,
        parameters: []
    },
    filterState: {
        id: "",
        health: null,
        throughPutTime: 0,
        totalThroughputTime: 0,
        status: FilterStatus.Unknown
    },
    extractFinish: false,
    updateConfiguration: false,
    resultAvailable: false,
    exampleDatasets: [],
    resultDatasets: [],
    currentExampleDataset: {
        metaData: "",
        records: [
            {
                id: "b8f4e010-dbe5-40ae-bd2c-b73a953da100",
                payload: [
                    { jsonClass: "TextField", name: "message",
                        value: "The weather is cloudy with a current temperature of: -11.5 C"},
                    { jsonClass: "NumberField", name: "temperature", value: 11.5}
                ]
            }
        ]
    }
};

export function FilterReducer(state: FilterState = initialState, action: FiltersActions): FilterState {

    const result: FilterState = Object.assign({}, state);

    switch (action.type) {
        case LOAD_LIVE_EDITING_FILTER_SUCCESS:
            result.updateConfiguration = false;
            result.filter = action.filter;
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
        case EXTRACT_DATASETS_INITIAL_SUCCESS:
            result.exampleDatasets = [];
            result.exampleDatasets = action.datasets;
            result.extractFinish = true;
            break;
        case EXTRACT_DATASETS_RESULT_SUCCESS:
            result.resultAvailable = true;
            result.resultDatasets = [];
            result.resultDatasets = action.datasets;
            result.extractFinish = true;
            break;
        case LOCK_CURRENT_EXAMPLE_DATASET:
            result.currentExampleDataset = action.dataset;
            break;
        case UPDATE_FILTER_CONFIGURATION:
            result.updateConfiguration = true;
            result.filter = deepcopy(action.filter);
            result.filter.parameters.forEach((p) => {
                if (p.jsonClass === "IntParameter") {
                    p.value = +action.values[p.name];
                } else {
                    p.value = action.values[p.name];
                }
            });
            break;
    }
    return result;
}

export const extractFinish = (state: FilterState) => state.extractFinish;

export const resultAvailable = (state: FilterState) => state.resultAvailable;

export const getUpdateConfigurationFlag = (state: FilterState) => state.updateConfiguration;

export const getFilterState = createFeatureSelector<FilterState>(
    "filter"
);
export const selectFilterId = createSelector(getFilterState,
    (state: FilterState) => state.filter.id);

export const selectLiveEditingFilter = createSelector(getFilterState, (state: FilterState) => state.filter);

export const selectLiveEditingFilterState = createSelector(getFilterState, (state: FilterState) => state.filterState);

export const selectExtractedDatasets = createSelector(getFilterState, (state: FilterState) => state.exampleDatasets);

export const selectExtractFinish = createSelector(getFilterState, extractFinish);

export const selectResultAvailable = createSelector(getFilterState, resultAvailable);

export const selectUpdateConfigurationFlag = createSelector(getFilterState, getUpdateConfigurationFlag);
