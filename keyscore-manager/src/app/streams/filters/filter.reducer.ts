import {FilterState} from "../streams.model"
import {FiltersActions, LOAD_FILTER_DESCRIPTOR_FAILURE, LOAD_FILTER_DESCRIPTOR_SUCCESS} from "./filters.actions";

const initialState: FilterState = {
    currentFilter: {
        name: '',
        displayName: '',
        description: '',
        previousConnection: undefined,
        nextConnection: undefined,
        parameters: [],
        category: ''
    }

};

export function FilterReducer(state: FilterState = initialState, action: FiltersActions): FilterState {

    const result: FilterState = Object.assign({}, state);

    switch (action.type) {
        case LOAD_FILTER_DESCRIPTOR_SUCCESS:
            result.currentFilter = action.filterDescriptor;
            console.log("set state");
            break;
        case LOAD_FILTER_DESCRIPTOR_FAILURE:
            console.log("set state failure");
            break;
    }
    return result;
}
