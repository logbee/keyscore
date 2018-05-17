import {FilterState} from "../streams.model"
import {
    FiltersActions,
    LOAD_FILTER_DESCRIPTOR_FAILURE,
    LOAD_FILTER_DESCRIPTOR_SUCCESS,
    LOAD_FILTER_MODEL_FROM_STREAM
} from "./filters.actions";

const initialState: FilterState = {
    currentFilter: {
        id:'',
        name: '',
        displayName: '',
        description: '',
        parameters: [],
    }

};

export function FilterReducer(state: FilterState = initialState, action: FiltersActions): FilterState {

    const result: FilterState = Object.assign({}, state);

    switch (action.type) {
        case LOAD_FILTER_MODEL_FROM_STREAM:
            result.currentFilter = action.filterModel
    }
    return result;
}
