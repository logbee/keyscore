import {FilterState} from "./filter-model";
import {FilterConnection, FilterModel} from "../streams/streams.model"
import {FiltersActions, SET_CURRENT_FILTER} from "./filters.actions";

const initialState: FilterState = {
    filter: {
        name: '',
        displayName: '',
        description: '',
        previousConnection: undefined,
        nextConnection: undefined,
        parameters: [],
        category: ''
    }

};

export function FilterReducer(state: FilterState = initialState, action: FiltersActions) {

    const result: FilterState = Object.assign({}, state);

    switch (action.type) {
        case SET_CURRENT_FILTER:
            result.filter = action.filterDescriptor;
            break;

    }
}
