import {deepcopy} from "../util";
import {
    CREATE_PIPELINE,
    DELETE_PIPELINE_FAILURE,
    DELETE_PIPELINE_SUCCESS,
    EDIT_PIPELINE_FAILURE,
    EDIT_PIPELINE_SUCCESS,
    LOAD_ALL_PIPELINES_SUCCESS, LOAD_FILTER_DESCRIPTORS_SUCCESS,
    PipelineActions,
    RESET_PIPELINE, RESOLVE_FILTER_DESCRIPTORS_SUCCESS,
    UPDATE_PIPELINE_FAILURE,
    UPDATE_PIPELINE_POLLING,
    UPDATE_PIPELINE_SUCCESS,
} from "./pipelines.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {Health} from "../models/common/Health";
import {PipelineInstance} from "../models/pipeline-model/PipelineInstance";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {Descriptor} from "../models/descriptors/Descriptor";
import {EditingPipelineModel} from "../models/pipeline-model/EditingPipelineModel";
import {ResolvedCategory} from "../models/descriptors/Category";
import {generateRef} from "../models/common/Ref";
import {StringTMap} from "../common/object-maps";

export class PipelinesState {
    public pipelineList: PipelineInstance[];
    public editingPipeline: EditingPipelineModel;
    public descriptors: Descriptor[];
    public filterDescriptors: ResolvedFilterDescriptor[];
    public filterCategories: ResolvedCategory[];
    public pipelineInstancePolling: boolean;
    public wasLastUpdateSuccessful: boolean[];
}

export const initialState: PipelinesState = {
    pipelineList: [],
    editingPipeline: null,
    descriptors: [],
    filterDescriptors: [],
    filterCategories: [],
    pipelineInstancePolling: false,
    wasLastUpdateSuccessful: []
};

export function PipelinesReducer(state: PipelinesState = initialState, action: PipelineActions): PipelinesState {

    const result: PipelinesState = Object.assign({}, state);

    switch (action.type) {
        case RESOLVE_FILTER_DESCRIPTORS_SUCCESS:
            result.filterDescriptors = action.resolvedDescriptors;
            result.filterCategories = result.filterDescriptors.map((descriptor) =>
                descriptor.categories).reduce((acc, val) => acc.concat(val), [])
                .filter((category, i, array) => array.indexOf(category) === i);
            break;
        case LOAD_FILTER_DESCRIPTORS_SUCCESS:
            result.descriptors = action.descriptors;
            break;
        case CREATE_PIPELINE:
        case EDIT_PIPELINE_FAILURE:
            result.editingPipeline = {
                pipelineBlueprint: {
                    ref: generateRef(),
                    blueprints: [],
                    metadata: {
                        labels: [{
                            name: "displayName",
                            value: {
                                jsonClass: "",
                                value: "New Pipeline"
                            }
                        }, {
                            name: "description",
                            value: {
                                jsonClass: "",
                                value: "Your new Pipeline"
                            }
                        }
                        ]
                    }
                },
                blueprints: [],
                configurations: []
            };
            break;
        case EDIT_PIPELINE_SUCCESS:
            result.editingPipeline = {
                pipelineBlueprint: action.pipelineBlueprint,
                blueprints: action.blueprints,
                configurations: action.configurations
            };
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

    }

    return result;
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

export const getLastUpdateSuccess = createSelector(getPipelinesState,
    (state: PipelinesState) => state.wasLastUpdateSuccessful);
