import {ParameterDescriptor, StreamsState} from "./streams.model";
import {
    ADD_FILTER,
    CREATE_STREAM,
    DELETE_STREAM, DELETE_STREAM_FAILURE, DELETE_STREAM_SUCCESS,
    EDIT_FILTER,
    EDIT_STREAM,
    LOAD_FILTER_DESCRIPTORS_SUCCESS,
    LOCK_EDITING_STREAM,
    MOVE_FILTER,
    REMOVE_FILTER,
    RESET_STREAM,
    StreamActions,
    UPDATE_FILTER,
    UPDATE_STREAM, UPDATE_STREAM_SUCCESS
} from "./streams.actions";
import {v4 as uuid} from 'uuid';
import {deepcopy} from "../util";


const initialState: StreamsState = {
    streamList: [
        {
            id: '64ca08cf-a80e-46b3-aa73-977ba743d332',
            name: 'Test',
            description: 'My Stream',
            filters: [
                {
                    id: '850c08cf-b88e-46b3-aa73-8877a743d443',
                    name: 'KafkaInput',
                    description: '',
                    displayName: 'Kafka Input',
                    parameters: []
                },
                {
                    id: 'ca4108cf-aaf4-5671-aa73-1717a743d215',
                    name: 'KafkaOutput',
                    description: '',
                    displayName: 'Kafka Output',
                    parameters: []
                }
            ]
        }
    ],
    editingStream: null,
    editingFilter: null,
    loading: false,
    filterDescriptors: [],
    filterCategories: [],
    editingStreamIsLocked: true
};

export function StreamsReducer(state: StreamsState = initialState, action: StreamActions): StreamsState {

    const result: StreamsState = Object.assign({}, state);

    switch (action.type) {
        case CREATE_STREAM:
            result.streamList.push({
                id: action.id,
                name: action.name,
                description: action.description,
                filters: []
            });
            break;
        case EDIT_STREAM:
            setEditingStream(result, action.id);
            break;
        case LOCK_EDITING_STREAM:
            result.editingStreamIsLocked = action.isLocked;
            break;
        case RESET_STREAM:
            setEditingStream(result, action.id);
            break;
        case UPDATE_STREAM_SUCCESS:
            const index = result.streamList.findIndex(stream => action.stream.id == stream.id);
            if (index >= 0) {
                result.streamList[index] = deepcopy(action.stream);
            }
            if (result.editingStream != null && result.editingStream.id == action.stream.id) {
                result.editingStream = deepcopy(action.stream);
            }
            break;
        case DELETE_STREAM_SUCCESS:
            result.streamList = result.streamList.filter(stream => action.id != stream.id);
            break;
        case DELETE_STREAM_FAILURE:
            if (action.cause.status == 404) {
                result.streamList = result.streamList.filter(stream => action.id != stream.id);
            }
            break;
        case ADD_FILTER:
            result.editingStream.filters.push({
                id: uuid(),
                name: action.filter.name,
                displayName: action.filter.displayName,
                description: action.filter.description,
                parameters: action.filter.parameters
            });
            break;
        case MOVE_FILTER:
            const filterIndex = result.editingStream.filters.findIndex(filter => filter.id == action.filterId);
            swap(result.editingStream.filters, filterIndex, action.position);
            break;
        case EDIT_FILTER:
            setEditingFilter(result, action.filterId);
            break;
        case UPDATE_FILTER:
            const updateFilterIndex = result.editingStream.filters.findIndex(filter => filter.id == action.filter.id);
            result.editingStream.filters[updateFilterIndex] = deepcopy(action.filter);
            result.editingStream.filters[updateFilterIndex].parameters.forEach(p => p.value = action.values[p.displayName]);
            break;
        case REMOVE_FILTER:
            const removeIndex = result.editingStream.filters.findIndex(filter => filter.id == action.filterId);
            result.editingStream.filters.splice(removeIndex, 1);
            break;
        case LOAD_FILTER_DESCRIPTORS_SUCCESS:
            result.filterDescriptors = action.descriptors;
            result.filterCategories = result.filterDescriptors.map(descriptor => descriptor.category).filter((category, index, array) => array.indexOf(category) == index);
    }

    return result
}

function setEditingStream(state: StreamsState, id: string) {
    //state.editingStream = Object.assign({}, state.streamList.find(stream => id == stream.id));
    state.editingStream = deepcopy(state.streamList.find(stream => id == stream.id));
}

function setEditingFilter(state: StreamsState, id: string) {
    state.editingFilter = deepcopy(state.editingStream.filters.find(f => f.id == id));
}

function moveElement<T>(arr: Array<T>, from: number, to: number) {
    arr.splice(to, 0, arr.splice(from, 1)[0]);
}

function swap<T>(arr: Array<T>, a: number, b: number) {
    if (a >= 0 && a < arr.length && b >= 0 && b < arr.length) {
        const temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
}