import {
    DRAIN_FILTER_SUCCESS,
    EXTRACT_DATASETS_SUCCESS,
    FiltersActions,
    LOAD_FILTERSTATE_SUCCESS,
    LOAD_LIVE_EDITING_FILTER_SUCCESS,
    LOCK_CURRENT_EXAMPLE_DATASET,
    PAUSE_FILTER_SUCCESS
} from "./filters.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {FilterConfiguration} from "../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../models/filter-model/FilterInstanceState";
import {FilterStatus} from "../models/filter-model/FilterStatus";
import {Dataset} from "../models/filter-model/dataset/Dataset";

export class FilterState {
    public filter: FilterConfiguration;
    public filterState: FilterInstanceState;
    public datasets: Dataset[];
    public extractFinish: boolean;
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
    resultAvailable: false,
    datasets: [{
        metaData: "",
        records: [
            {
                id: "b8f4e010-dbe5-40ae-bd2c-b73a953da100",
                payload: {
                    message: {
                        jsonClass: "TextField",
                        name: "message",
                        value: "The weather is cloudy with a current temperature of: -11.5 C"
                    }
                }
            }
        ]
    }],
    currentExampleDataset: {
        metaData: "",
        records: [
            {
                id: "b8f4e010-dbe5-40ae-bd2c-b73a953da100",
                payload: {
                    message: {
                        jsonClass: "TextField",
                        name: "message",
                        value: "The weather is cloudy with a current temperature of: -11.5 C"
                    }
                }
            }
        ]
    }
};

export function FilterReducer(state: FilterState = initialState, action: FiltersActions): FilterState {

    const result: FilterState = Object.assign({}, state);

    switch (action.type) {
        case LOAD_LIVE_EDITING_FILTER_SUCCESS:
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
        case EXTRACT_DATASETS_SUCCESS:
            result.datasets = [];
            result.datasets = action.datasets;
            result.extractFinish = true;
            break;
        case LOCK_CURRENT_EXAMPLE_DATASET:
            result.currentExampleDataset = action.dataset;
    }
    return result;
}

export const extractFinish = (state: FilterState) => state.extractFinish;

export const resultAvailable = (state: FilterState) => state.resultAvailable;

export const getFilterState = createFeatureSelector<FilterState>(
    "filter"
);
export const getFilterId = createSelector(getFilterState,
    (state: FilterState) => state.filter.id);

export const getLiveEditingFilter = createSelector(getFilterState, (state: FilterState) => state.filter);

export const getLiveEditingFilterState = createSelector(getFilterState, (state: FilterState) => state.filterState);

export const getExtractedDatasets = createSelector(getFilterState, (state: FilterState) => state.datasets);

export const getExtractFinish = createSelector(getFilterState, extractFinish);

export const getResultAvailable = createSelector(getFilterState, resultAvailable);
