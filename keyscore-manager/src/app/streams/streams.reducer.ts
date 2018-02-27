import {StreamsState} from "./streams.model";
import {
    ADD_FILTER,
    CREATE_STREAM,
    DELETE_STREAM, EDIT_FILTER,
    EDIT_STREAM,
    LOAD_FILTER_DESCRIPTORS_SUCCESS,
    MOVE_FILTER,
    RESET_STREAM,
    StreamActions,
    UPDATE_STREAM
} from "./streams.actions";
import {v4 as uuid} from 'uuid';


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
                    displayName:'Kafka Input',
                    parameters:[],
                    isEdited:false
                },
                {
                    id: 'ca4108cf-aaf4-5671-aa73-1717a743d215',
                    name: 'KafkaOutput',
                    description: '',
                    displayName:'Kafka Output',
                    parameters:[],
                    isEdited:false
                }
            ]
        }
    ],
    editingStream: null,
    loading: false,
    filterDescriptors:[],
    filterCategories:[]
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
        case RESET_STREAM:
            setEditingStream(result, action.id);
            break;
        case UPDATE_STREAM:
            const index = result.streamList.findIndex(stream => action.stream.id == stream.id);
            if (index >= 0) {
                result.streamList[index] = action.stream;
            }
            if (result.editingStream != null && result.editingStream.id == action.stream.id) {
                result.editingStream = action.stream;
            }
            break;
        case DELETE_STREAM:
            result.streamList = result.streamList.filter(stream => action.id != stream.id);
            break;
        case ADD_FILTER:
            result.editingStream.filters.push({
                id: uuid(),
                name: action.filter.name,
                displayName:action.filter.displayName,
                description: action.filter.description,
                parameters: action.filter.parameters,
                isEdited:false
            });
            break;
        case MOVE_FILTER:
            const filterIndex = result.editingStream.filters.findIndex(filter => filter.id == action.filterId);
            swap(result.editingStream.filters, filterIndex, action.position);
            break;
        case EDIT_FILTER:
            result.streamList.forEach(stream => stream.filters.forEach(f => f.isEdited = false))
            result.editingStream.filters.find(f=> f.id == action.filterId).isEdited = true;
            break;
        case LOAD_FILTER_DESCRIPTORS_SUCCESS:
            result.filterDescriptors = action.descriptors;
            result.filterCategories = result.filterDescriptors.map(descriptor => descriptor.category).filter((category,index,array) => array.indexOf(category) == index);
    }

    return result
}

function setEditingStream(state: StreamsState, id: string) {
    state.editingStream = Object.assign({}, state.streamList.find(stream => id == stream.id));
}

function swap<T>(arr: Array<T>, a: number, b: number) {
    if (a >= 0 && a < arr.length && b >= 0 && b < arr.length) {
        const temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
}