import {deepcopy} from "../util";
import {
    CREATE_PIPELINE,
    DELETE_PIPELINE_FAILURE,
    DELETE_PIPELINE_SUCCESS,
    EDIT_PIPELINE_FAILURE,
    EDIT_PIPELINE_SUCCESS,
    LOAD_ALL_PIPELINES_SUCCESS,
    LOAD_FILTER_DESCRIPTORS_SUCCESS,
    PipelineActions,
    RESET_PIPELINE,
    RESOLVE_FILTER_DESCRIPTORS_SUCCESS,
    UPDATE_PIPELINE_FAILURE,
    UPDATE_PIPELINE_POLLING,
    UPDATE_PIPELINE_SUCCESS,
} from "./pipelines.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {PipelineInstance} from "../models/pipeline-model/PipelineInstance";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {Descriptor} from "../models/descriptors/Descriptor";
import {EditingPipelineModel, generateEmptyEditingPipelineModel} from "../models/pipeline-model/EditingPipelineModel";
import {ResolvedCategory} from "../models/descriptors/Category";

export class PipelinesState {
    public pipelineList: PipelineInstance[];
    public editingPipeline: EditingPipelineModel;
    public descriptors: Descriptor[];
    public filterDescriptors: ResolvedFilterDescriptor[];
    public filterCategories: ResolvedCategory[];
    public pipelineInstancePolling: boolean;
}

export const initialState: PipelinesState = {
    pipelineList: [],
    editingPipeline: null,
    descriptors: [],
    filterDescriptors: [],
    filterCategories: [],
    pipelineInstancePolling: false,
};

export function PipelinesReducer(state: PipelinesState = initialState, action: PipelineActions): PipelinesState {

    switch (action.type) {
        case RESOLVE_FILTER_DESCRIPTORS_SUCCESS:
            let filterCategories = action.resolvedDescriptors.map((descriptor) =>
                descriptor.categories).reduce((acc, val) => acc.concat(val), [])
                .filter((category, i, array) => array.indexOf(category) === i);
            return {...state, filterDescriptors: action.resolvedDescriptors, filterCategories: filterCategories};
        case LOAD_FILTER_DESCRIPTORS_SUCCESS:
            return {...state, descriptors: action.descriptors};
        case CREATE_PIPELINE:
            return {...state, editingPipeline: generateEmptyEditingPipelineModel({uuid: action.id})};
        case EDIT_PIPELINE_SUCCESS:
            let editingPipeline = {
                pipelineBlueprint: action.pipelineBlueprint,
                blueprints: action.blueprints,
                configurations: action.configurations
            };
            return {...state, editingPipeline: editingPipeline};
        case RESET_PIPELINE:
            return state;
        case UPDATE_PIPELINE_SUCCESS:
            return state;
        case UPDATE_PIPELINE_FAILURE:
            return state;
        case DELETE_PIPELINE_SUCCESS:
            let pipelineList = deepcopy(state.pipelineList, []).filter((pipeline) => action.id !== pipeline.id);
            return {...state, pipelineList: pipelineList};
        case DELETE_PIPELINE_FAILURE:
            if (action.cause.status === 404) {
                let pipelineList = deepcopy(state.pipelineList, []).filter((pipeline) => action.id !== pipeline.id);
                return {...state, pipelineList: pipelineList};
            }
            return state;
        case LOAD_ALL_PIPELINES_SUCCESS:
            action.pipelineInstances.sort((a, b) => {
                return a.name.localeCompare(b.name);
            });
            return {...state, pipelineList: action.pipelineInstances};
        case UPDATE_PIPELINE_POLLING:
            return {...state, pipelineInstancePolling: action.isPolling};
        default:
            return state;

    }
}

export const getPipelinesState = createFeatureSelector<PipelinesState>(
    "pipelines"
);
export const getPipelineList = createSelector(getPipelinesState,
    (state: PipelinesState) => state.pipelineList);

export const getEditingPipeline = createSelector(getPipelinesState,
    (state: PipelinesState) => state.editingPipeline);

export const getFilterDescriptors = createSelector(getPipelinesState,
    (state: PipelinesState) => state.filterDescriptors);

export const getFilterCategories = createSelector(getPipelinesState,
    (state: PipelinesState) => state.filterCategories);

export const getPipelinePolling = createSelector(getPipelinesState,
    (state: PipelinesState) => state.pipelineInstancePolling);
