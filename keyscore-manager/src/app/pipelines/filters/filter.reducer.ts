import {FilterState} from "../pipelines.model";
import {FiltersActions, SET_LIVE_EDITING_FILTER} from "./filters.actions";

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
        case SET_LIVE_EDITING_FILTER:
            console.log("Set new State with:" + action.filter);
            result.filter = action.filter;
    }
    return result;
}
