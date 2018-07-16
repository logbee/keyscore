import {FiltersActions, LOAD_FILTERSTATE_SUCCESS, LOAD_LIVE_EDITING_FILTER_SUCCESS} from "./filters.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {FilterConfiguration} from "../../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../../models/filter-model/FilterInstanceState";
import {FilterStatus} from "../../models/filter-model/FilterStatus";

export class FilterState {
    public filter: FilterConfiguration;
    public filterState: FilterInstanceState;
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

    }
    return result;
}

export const getFilterState = createFeatureSelector<FilterState>(
    "filter"
);
export const getFilterId = createSelector(getFilterState,
    (state: FilterState) => state.filter.id);

export const getLiveEditingFilter = createSelector(getFilterState, (state: FilterState) => state.filter);

export const getLiveEditingFilterState = createSelector(getFilterState, (state: FilterState) => state.filterState);
