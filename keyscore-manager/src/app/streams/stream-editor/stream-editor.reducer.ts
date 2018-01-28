import {
    ADD_FILTER,
    DISABLE_FILTER,
    EDIT_FILTER,
    ENABLE_FILTER,
    MOVE_FILTER,
    REMOVE_FILTER,
    SAVE_FILTER,
    StreamEditorActions
} from "./stream-editor.actions";

export class Stream {
    name: String;
    description: String;
    filters: FilterInstance[];
}

export class FilterInstance {
    constructor(public id: number,
                public name: String,
                public description: String = '',
                public serverId: String = '',
                public editing: boolean = false,
                public enabled: boolean = true) {
    }
}

export const initialState: Stream = {
    name: 'Test Stream',
    description: 'This is a test stream.',
    filters: [
        new FilterInstance(1, 'Kafka Input', 'Where does it come from?'),
        new FilterInstance(2, 'Drop crap', 'Drop the most'),
        new FilterInstance(3, 'Add fields', 'There are very interesting fields!'),
        new FilterInstance(4, 'Kafka Output', ''),
    ]
};

export function streamEditorReducer(state: Stream = initialState, action: StreamEditorActions): Stream {

    const result: Stream = Object.assign({}, state);

    switch (action.type) {
        case REMOVE_FILTER:
            result.filters = state.filters.filter(filter => filter.id != action.filterId);
            return result;
        case MOVE_FILTER:
            const index = state.filters.findIndex(filter => filter.id == action.filterId);
            swap(result.filters, index, action.position);
            return result;
        case EDIT_FILTER:
            result.filters.find(filter => filter.id == action.filterId).editing = true;
            return result;
        case SAVE_FILTER:
            result.filters.find(filter => filter.id == action.filterId).editing = false;
            return result;
        case ENABLE_FILTER:
            result.filters.find(filter => filter.id == action.filterId).enabled = true;
            return result;
        case DISABLE_FILTER:
            result.filters.find(filter => filter.id == action.filterId).enabled = false;
            return result;
        case ADD_FILTER:
            result.filters.push(new FilterInstance(getNewFilterId(result.filters), action.filter.displayName, action.filter.description));
        default:
            return result;
    }
}

function swap<T>(arr: Array<T>, a: number, b: number) {
    if (a >= 0 && a < arr.length && b >= 0 && b < arr.length) {
        const temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
}

function getNewFilterId(arr: FilterInstance[]) {
    let maxVal: number = 0;
    arr.forEach(function getMaxValue(filter, i, restArray) {
        if (filter.id > maxVal) {
            maxVal = filter.id
        }
    });
    return maxVal++;
}