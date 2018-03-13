import {
    BooleanParameter,
    IntParameter,
    ListParameter, ListParameterDescriptor, MapParameter, MapParameterDescriptor, Parameter,
    ParameterDescriptor,
    StreamsState, TextListParameter, TextMapParameter,
    TextParameter
} from "./streams.model";
import {
    ADD_FILTER,
    CREATE_STREAM,
    DELETE_STREAM, EDIT_FILTER,
    EDIT_STREAM,
    LOAD_FILTER_DESCRIPTORS_SUCCESS, LOCK_EDITING_STREAM,
    MOVE_FILTER, REMOVE_FILTER,
    RESET_STREAM,
    StreamActions, UPDATE_FILTER,
    UPDATE_STREAM
} from "./streams.actions";
import {v4 as uuid} from 'uuid';
import 'jquery';


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
        case UPDATE_STREAM:
            const index = result.streamList.findIndex(stream => action.stream.id == stream.id);
            if (index >= 0) {
                result.streamList[index] = jQuery.extend(true, {}, action.stream);
            }
            if (result.editingStream != null && result.editingStream.id == action.stream.id) {
                result.editingStream = jQuery.extend(true, {}, action.stream);
            }
            break;
        case DELETE_STREAM:
            result.streamList = result.streamList.filter(stream => action.id != stream.id);
            break;
        case ADD_FILTER:
            result.editingStream.filters.push({
                id: uuid(),
                name: action.filter.name,
                displayName: action.filter.displayName,
                description: action.filter.description,
                parameters: createParametersFromDescriptors(action.filter.parameters)
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
            result.editingStream.filters[updateFilterIndex] = jQuery.extend(true,{},action.filter);
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
    state.editingStream = jQuery.extend(true, {}, state.streamList.find(stream => id == stream.id));
}

function setEditingFilter(state: StreamsState, id: string) {
    state.editingFilter = Object.assign({}, state.editingStream.filters.find(f => f.id == id));
}

function createParametersFromDescriptors(parameterDescriptors: ParameterDescriptor[]): Parameter[] {
    return parameterDescriptors.map(p => {
        switch (p.kind) {
            case 'text':
                return p as TextParameter;
            case 'int':
                return p as IntParameter;
            case 'boolean':
                return p as BooleanParameter;
            case 'list':
                return createListParameterFromDescriptor(p as ListParameterDescriptor);
            case 'map':
                return createMapParameterFromDescriptor(p as MapParameterDescriptor);


        }

    })
}

function createListParameterFromDescriptor(p: ListParameterDescriptor): ListParameter {
    if (p.element.kind === 'text') {
        let element = p.element as TextParameter;
        return {
            name: p.name, displayName: p.displayName, mandatory: p.mandatory,
            kind: 'list[string]', min: p.min, max: p.max, validator: element.validator, value: []
        } as TextListParameter;
    }
}

function createMapParameterFromDescriptor(p: MapParameterDescriptor): MapParameter {
    if (p.key.kind === 'text' && p.value.kind === 'text') {
        let key = p.key as TextParameter;
        let value = p.value as TextParameter;
        return {
            name: p.name,
            displayName: p.displayName,
            mandatory: p.mandatory,
            kind: 'map[string,string]',
            min: p.min,
            max: p.max,
            keyValidator: key.validator,
            valueValidator: value.validator
        } as TextMapParameter;
    }
}

function swap<T>(arr: Array<T>, a: number, b: number) {
    if (a >= 0 && a < arr.length && b >= 0 && b < arr.length) {
        const temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
}