import {PipelinesState} from "./pipelines.model";
import {
    ADD_FILTER,
    CREATE_PIPELINE,
    DELETE_PIPELINE_FAILURE,
    DELETE_PIPELINE_SUCCESS,
    EDIT_PIPELINE,
    LOAD_FILTER_DESCRIPTORS_SUCCESS,
    LOCK_EDITING_PIPELINE,
    MOVE_FILTER,
    PipelineActions,
    REMOVE_FILTER,
    RESET_PIPELINE,
    UPDATE_FILTER,
    UPDATE_PIPELINE_SUCCESS
} from "./pipelines.actions";
import {v4 as uuid} from 'uuid';
import {deepcopy} from "../util";


const initialState: PipelinesState = {
    pipelineList: [
        {
            id: '64ca08cf-a80e-46b3-aa73-977ba743d332',
            name: 'Test',
            description: 'My Pipeline',
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
    editingPipeline: null,
    editingFilter: null,
    loading: false,
    filterDescriptors: [],
    filterCategories: [],
    editingPipelineIsLocked: true
};

export function PipelinesReducer(state: PipelinesState = initialState, action: PipelineActions): PipelinesState {

    const result: PipelinesState = Object.assign({}, state);

    switch (action.type) {
        case CREATE_PIPELINE:
            result.pipelineList.push({
                id: action.id,
                name: action.name,
                description: action.description,
                filters: []
            });
            break;
        case EDIT_PIPELINE:
            setEditingPipeline(result, action.id);
            break;
        case LOCK_EDITING_PIPELINE:
            result.editingPipelineIsLocked = action.isLocked;
            break;
        case RESET_PIPELINE:
            setEditingPipeline(result, action.id);
            break;
        case UPDATE_PIPELINE_SUCCESS:
            const index = result.pipelineList.findIndex(pipeline => action.pipeline.id == pipeline.id);
            if (index >= 0) {
                result.pipelineList[index] = deepcopy(action.pipeline);
            }
            if (result.editingPipeline != null && result.editingPipeline.id == action.pipeline.id) {
                result.editingPipeline = deepcopy(action.pipeline);
            }
            break;
        case DELETE_PIPELINE_SUCCESS:
            result.pipelineList = result.pipelineList.filter(pipeline => action.id != pipeline.id);
            break;
        case DELETE_PIPELINE_FAILURE:
            if (action.cause.status == 404) {
                result.pipelineList = result.pipelineList.filter(pipeline => action.id != pipeline.id);
            }
            break;
        case ADD_FILTER:
            result.editingPipeline.filters.push({
                id: uuid(),
                name: action.filter.name,
                displayName: action.filter.displayName,
                description: action.filter.description,
                parameters: action.filter.parameters
            });
            break;
        case MOVE_FILTER:
            const filterIndex = result.editingPipeline.filters.findIndex(filter => filter.id == action.filterId);
            swap(result.editingPipeline.filters, filterIndex, action.position);
            break;
        case UPDATE_FILTER:
            const updateFilterIndex = result.editingPipeline.filters.findIndex(filter => filter.id == action.filter.id);
            result.editingPipeline.filters[updateFilterIndex] = deepcopy(action.filter);
            result.editingPipeline.filters[updateFilterIndex].parameters.forEach(p => p.value = action.values[p.displayName]);
            break;
        case REMOVE_FILTER:
            const removeIndex = result.editingPipeline.filters.findIndex(filter => filter.id == action.filterId);
            result.editingPipeline.filters.splice(removeIndex, 1);
            break;
        case LOAD_FILTER_DESCRIPTORS_SUCCESS:
            result.filterDescriptors = action.descriptors;
            result.filterCategories = result.filterDescriptors.map(descriptor => descriptor.category).filter((category, index, array) => array.indexOf(category) == index);
    }

    return result
}

function setEditingPipeline(state: PipelinesState, id: string) {
    //state.editingPipeline = Object.assign({}, state.pipelineList.find(pipeline => id == pipeline.id));
    state.editingPipeline = deepcopy(state.pipelineList.find(pipeline => id == pipeline.id));
}

function setEditingFilter(state: PipelinesState, id: string) {
    state.editingFilter = deepcopy(state.editingPipeline.filters.find(f => f.id == id));
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