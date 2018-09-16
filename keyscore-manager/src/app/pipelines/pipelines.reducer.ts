import {v4 as uuid} from "uuid";
import {deepcopy, parameterDescriptorToParameter, toInternalPipelineConfig} from "../util";
import {
    ADD_FILTER,
    CREATE_PIPELINE,
    DELETE_PIPELINE_FAILURE,
    DELETE_PIPELINE_SUCCESS,
    EDIT_PIPELINE_FAILURE,
    EDIT_PIPELINE_SUCCESS,
    LOAD_ALL_PIPELINES_SUCCESS,
    LOAD_FILTER_DESCRIPTORS_SUCCESS,
    LOCK_EDITING_PIPELINE,
    MOVE_FILTER,
    PipelineActions,
    REMOVE_FILTER,
    RESET_PIPELINE,
    UPDATE_FILTER, UPDATE_PIPELINE_FAILURE,
    UPDATE_PIPELINE_POLLING,
    UPDATE_PIPELINE_SUCCESS,
} from "./pipelines.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {Health} from "../models/common/Health";
import {PipelineInstance} from "../models/pipeline-model/PipelineInstance";
import {FilterConfiguration} from "../models/filter-model/FilterConfiguration";
import {FilterDescriptor, ResolvedFilterDescriptor} from "../models/filter-model/FilterDescriptor";
import {InternalPipelineConfiguration} from "../models/pipeline-model/InternalPipelineConfiguration";
import {Descriptor} from "../models/descriptors/Descriptor";

export class PipelinesState {
    public pipelineList: PipelineInstance[];
    public editingPipeline: InternalPipelineConfiguration;
    public editingFilter: FilterConfiguration;
    public descriptors: Descriptor[];
    public filterDescriptors: ResolvedFilterDescriptor[];
    public filterCategories: string[];
    public editingPipelineIsLocked: boolean;
    public pipelineInstancePolling: boolean;
    public wasLastUpdateSuccessful: boolean[];
}
const initialState: PipelinesState = {
    pipelineList: [],
    editingPipeline: null,
    editingFilter: null,
    descriptors:[],
    filterDescriptors: [],
    filterCategories: [],
    editingPipelineIsLocked: true,
    pipelineInstancePolling: false,
    wasLastUpdateSuccessful: []
};

export function PipelinesReducer(state: PipelinesState = initialState, action: PipelineActions): PipelinesState {

    const result: PipelinesState = Object.assign({}, state);

    switch (action.type) {
        case CREATE_PIPELINE:
            result.editingPipeline = {
                id: action.id, name: action.name, description: action.description, filters: [],
                isRunning: false
            };
            break;
        case EDIT_PIPELINE_SUCCESS:
            result.editingPipeline = {...toInternalPipelineConfig(action.pipelineConfiguration), isRunning: false};
            result.wasLastUpdateSuccessful = [];
            break;
        case EDIT_PIPELINE_FAILURE:
            result.editingPipeline = {
                id: action.id, name: "New Pipeline", description: "", filters: [],
                isRunning: false
            };
            result.wasLastUpdateSuccessful = [];
            break;
        case LOCK_EDITING_PIPELINE:
            result.editingPipelineIsLocked = action.isLocked;
            break;
        case RESET_PIPELINE:
            break;
        case UPDATE_PIPELINE_SUCCESS:
            const index = result.pipelineList.findIndex((pipeline) => action.pipeline.id === pipeline.id);
            if (index >= 0) {
                result.pipelineList[index].name = action.pipeline.name;
                result.pipelineList[index].description = action.pipeline.description;
                result.pipelineList[index].health = Health.Red;
            } else {
                result.pipelineList.push({
                    id: action.pipeline.id,
                    name: action.pipeline.name,
                    description: action.pipeline.description,
                    configurationId: action.pipeline.id,
                    health: Health.Red,
                });
            }
            result.wasLastUpdateSuccessful = [true];

            break;
        case UPDATE_PIPELINE_FAILURE:

            result.wasLastUpdateSuccessful = [false];
            break;
        case DELETE_PIPELINE_SUCCESS:
            result.pipelineList = result.pipelineList.filter((pipeline) => action.id !== pipeline.id);
            break;
        case DELETE_PIPELINE_FAILURE:
            if (action.cause.status === 404) {
                result.pipelineList = result.pipelineList.filter((pipeline) => action.id !== pipeline.id);
            }
            break;
        case LOAD_ALL_PIPELINES_SUCCESS:
            result.pipelineList = deepcopy(action.pipelineInstances, []);
            result.pipelineList.sort((a, b) => {
                return a.name.localeCompare(b.name);
            });
            break;
        case UPDATE_PIPELINE_POLLING:
            result.pipelineInstancePolling = action.isPolling;
            break;
            //commented due to api change
        /*case ADD_FILTER:
            const parameters = action.filter.parameters.map((parameterDescriptor) =>
                parameterDescriptorToParameter(parameterDescriptor));
            result.editingPipeline.filters.push({
                id: uuid(),
                descriptor: action.filter,
                parameters,
            });
            break;*/
        case MOVE_FILTER:
            const filterIndex = result.editingPipeline.filters.findIndex((filter) =>
                filter.id === action.filterId);
            swap(result.editingPipeline.filters, filterIndex, action.position);
            break;
        case UPDATE_FILTER:
            const updateFilterIndex = result.editingPipeline.filters.findIndex((filter) =>
                filter.id === action.filter.id);
            result.editingPipeline.filters[updateFilterIndex] = deepcopy(action.filter);
            result.editingPipeline.filters[updateFilterIndex].parameters.forEach((p) => {
                if (p.jsonClass === "IntParameter") {
                    p.value = +action.values[p.name];
                } else {
                    p.value = action.values[p.name];
                }
            });
            break;
        case REMOVE_FILTER:
            const removeIndex = result.editingPipeline.filters.findIndex((filter) =>
                filter.id === action.filterId);
            result.editingPipeline.filters.splice(removeIndex, 1);
            break;
        /*case LOAD_FILTER_DESCRIPTORS_SUCCESS:
            result.filterDescriptors = action.descriptors;
            result.filterCategories = result.filterDescriptors.map((descriptor) =>
                descriptor.category).filter((category, i, array) => array.indexOf(category) === i);
            break;*/
    }

    return result;
}

function swap<T>(arr: T[], a: number, b: number) {
    if (a >= 0 && a < arr.length && b >= 0 && b < arr.length) {
        const temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
}

export const getPipelinesState = createFeatureSelector<PipelinesState>(
    "pipelines"
);
export const getPipelineList = createSelector(getPipelinesState,
    (state: PipelinesState) => state.pipelineList);

export const getEditingPipeline = createSelector(getPipelinesState,
    (state: PipelinesState) => state.editingPipeline);

export const getEditingPipelineIsLocked = createSelector(getPipelinesState,
    (state: PipelinesState) => state.editingPipelineIsLocked);

export const getFilterDescriptors = createSelector(getPipelinesState,
    (state: PipelinesState) => state.filterDescriptors);

export const getFilterCategories = createSelector(getPipelinesState,
    (state: PipelinesState) => state.filterCategories);

export const getEditingFilterParameters = createSelector(getPipelinesState,
    (state: PipelinesState) => state.editingFilter.parameters);

export const getEditingFilter = createSelector(getPipelinesState,
    (state: PipelinesState) => state.editingFilter);

export const getPipelinePolling = createSelector(getPipelinesState,
    (state: PipelinesState) => state.pipelineInstancePolling);

export const getLastUpdateSuccess = createSelector(getPipelinesState,
    (state: PipelinesState) => state.wasLastUpdateSuccessful);
