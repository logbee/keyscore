/*import {StreamEditorActions} from "./stream-editor.actions";
import {FilterModel, ParameterDescriptor, StreamModel} from "../streams.model";
import {v4 as uuid} from 'uuid';


export class Stream {
    name: string;
    description: string;
    filters: FilterInstance[];
}*/

/*export class FilterInstance implements FilterModel {
    constructor(public id: string,
                public name: string,

                public description: string = '',
                public serverId: string = '',
                public editing: boolean = false,
                public enabled: boolean = true,) {
    }
}

export const initialState: Stream = {
    name: 'Test Stream',
    description: 'This is a test stream.',
    filters: [
        new FilterInstance(uuid(), 'Kafka Input', 'Where does it come from?'),
        new FilterInstance(uuid(), 'Drop crap', 'Drop the most'),
        new FilterInstance(uuid(), 'Add fields', 'There are very interesting fields!'),
        new FilterInstance(uuid(), 'Kafka Output', ''),
    ]
};*/

/*export function StreamEditorReducer(state: StreamModel, action: StreamEditorActions): StreamModel {

    const result: StreamModel = Object.assign({}, state);
    return result;
    // switch (action.type) {
    //     case REMOVE_FILTER:
    //         result.filters = state.filters.filter(filter => filter.id != action.filterId);
    //         return result;
    //     case MOVE_FILTER:
    //         const index = state.filters.findIndex(filter => filter.id == action.filterId);
    //         swap(result.filters, index, action.position);
    //         return result;
    //     case EDIT_FILTER:
    //         result.filters.find(filter => filter.id == action.filterId).editing = true;
    //         return result;
    //     case SAVE_FILTER:
    //         result.filters.find(filter => filter.id == action.filterId).editing = false;
    //         return result;
    //     case ENABLE_FILTER:
    //         result.filters.find(filter => filter.id == action.filterId).enabled = true;
    //         return result;
    //     case DISABLE_FILTER:
    //         result.filters.find(filter => filter.id == action.filterId).enabled = false;
    //         return result;
    //     case ADD_FILTER:
    //         result.filters.push(new FilterInstance(getNewFilterId(result.filters), action.filter.displayName, action.filter.description));
    //     default:
    //         return result;
    // }
}

function swap<T>(arr: Array<T>, a: number, b: number) {
    if (a >= 0 && a < arr.length && b >= 0 && b < arr.length) {
        const temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
}*/

