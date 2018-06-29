import {Health, PipelinesState} from "./pipelines.model";
import {
    ADD_FILTER,
    CREATE_PIPELINE,
    DELETE_PIPELINE_FAILURE,
    DELETE_PIPELINE_SUCCESS,
    EDIT_PIPELINE, EDIT_PIPELINE_FAILURE, EDIT_PIPELINE_SUCCESS,
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
import {deepcopy, parameterDescriptorToParameter} from "../util";
import {State} from "@ngrx/store";
import {CONFIG_LOADED} from "../app.config";


const initialState: PipelinesState = {
    pipelineList: [],
    editingPipeline: null,
    editingFilter: null,
    loading: false,
    filterDescriptors: [],
    filterCategories: [],
    editingPipelineIsLocked: true,
};

export function PipelinesReducer(state: PipelinesState = initialState, action: PipelineActions): PipelinesState {

    const result: PipelinesState = Object.assign({}, state);

    switch (action.type) {
        case CREATE_PIPELINE:
            result.editingPipeline = {id: action.id, name: action.name, description: action.description, filters: []};
            break;
        case EDIT_PIPELINE_SUCCESS:
            result.editingPipeline = deepcopy(action.pipelineConfiguration);
            break;
        case EDIT_PIPELINE_FAILURE:
            result.editingPipeline = {id:action.id,name:"New Pipeline",description:"",filters:[]};
            break;
        case LOCK_EDITING_PIPELINE:
            result.editingPipelineIsLocked = action.isLocked;
            break;
        case RESET_PIPELINE:
            //setEditingPipeline(result, action.id);
            break;
        case UPDATE_PIPELINE_SUCCESS:
            const index = result.pipelineList.findIndex(pipeline => action.pipeline.id == pipeline.id);
            if (index >= 0) {
                result.pipelineList[index].name = action.pipeline.name;
                result.pipelineList[index].description = action.pipeline.description;
            } else {
                result.pipelineList.push({
                    id: action.pipeline.id,
                    name: action.pipeline.name,
                    description: action.pipeline.description,
                    configurationId: action.pipeline.id,
                    health: Health.Red
                })
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
            let parameters = action.filter.parameters.map(parameterDescriptor => parameterDescriptorToParameter(parameterDescriptor));
            result.editingPipeline.filters.push({
                id: uuid(),
                descriptor: action.filter,
                parameters: parameters
            });
            break;
        case MOVE_FILTER:
            const filterIndex = result.editingPipeline.filters.findIndex(filter => filter.id == action.filterId);
            swap(result.editingPipeline.filters, filterIndex, action.position);
            break;
        case UPDATE_FILTER:
            const updateFilterIndex = result.editingPipeline.filters.findIndex(filter => filter.id == action.filter.id);
            result.editingPipeline.filters[updateFilterIndex] = deepcopy(action.filter);
            result.editingPipeline.filters[updateFilterIndex].parameters.forEach(p => {
                if (p.jsonClass === 'int') {
                    p.value = +action.values[p.name];
                } else {
                    p.value = action.values[p.name];
                }
            });
            break;
        case REMOVE_FILTER:
            const removeIndex = result.editingPipeline.filters.findIndex(filter => filter.id == action.filterId);
            result.editingPipeline.filters.splice(removeIndex, 1);
            break;
        case LOAD_FILTER_DESCRIPTORS_SUCCESS:
            result.filterDescriptors = action.descriptors;
            result.filterCategories = result.filterDescriptors.map(descriptor => descriptor.category).filter((category, index, array) => array.indexOf(category) == index);
            break;
    }

    return result
}

function swap<T>(arr: Array<T>, a: number, b: number) {
    if (a >= 0 && a < arr.length && b >= 0 && b < arr.length) {
        const temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
}
