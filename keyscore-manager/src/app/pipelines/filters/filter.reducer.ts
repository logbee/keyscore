import {FilterState} from "../pipelines.model"
import {CONFIGURE_FILTER, FiltersActions, LOCK_FILTER} from "./filters.actions";
import {resetFakeAsyncZone} from "@angular/core/testing";

const initialState: FilterState = {
    filterId: ''
};

export function FilterReducer(state: FilterState = initialState, action: FiltersActions): FilterState {

    const result: FilterState = Object.assign({}, state);

    switch (action.type) {
        case CONFIGURE_FILTER:
            result.filterId = action.id;

        // case LOCK_FILTER:
        //     result.filter = action.filter
    }
    return result;
}
