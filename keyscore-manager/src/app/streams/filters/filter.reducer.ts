import {FilterState} from "../streams.model"
import {CONFIGURE_FILTER, FiltersActions} from "./filters.actions";

const initialState: FilterState = {
    filterId: ''
};

export function FilterReducer(state: FilterState = initialState, action: FiltersActions): FilterState {

    const result: FilterState = Object.assign({}, state);

    switch (action.type) {
        case CONFIGURE_FILTER:
            result.filterId = action.id;
    }
    return result;
}
