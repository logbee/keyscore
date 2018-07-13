import {FilterState, FilterStatus, Health} from "../pipelines.model";
import {
    FiltersActions, LOAD_FILTERSTATE, LOAD_FILTERSTATE_FAILURE, LOAD_FILTERSTATE_SUCCESS,
    LOAD_LIVE_EDITING_FILTER_FAILURE,
    LOAD_LIVE_EDITING_FILTER_SUCCESS
} from "./filters.actions";

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
        pipelineThroughput: 0,
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
