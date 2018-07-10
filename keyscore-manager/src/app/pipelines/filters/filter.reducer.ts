import {FilterState} from "../pipelines.model";
import {
    FiltersActions,
    LOAD_LIVE_EDITING_FILTER_FAILURE,
    LOAD_LIVE_EDITING_FILTER_SUCCESS
} from "./filters.actions";

const initialState: FilterState = {
    filter: {
        id: "",
        descriptor: null,
        parameters: []
    }
};

export function FilterReducer(state: FilterState = initialState, action: FiltersActions): FilterState {

    const result: FilterState = Object.assign({}, state);

    switch (action.type) {
        case LOAD_LIVE_EDITING_FILTER_SUCCESS:
            result.filter = action.filter;
            break;
        case LOAD_LIVE_EDITING_FILTER_FAILURE:
            if (action.cause.status === 404) {
                result.filter = {id: "", descriptor: null, parameters: []};
            }
            break;
    }
    return result;
}
