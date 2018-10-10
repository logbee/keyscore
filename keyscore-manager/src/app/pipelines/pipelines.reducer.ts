import {deepcopy} from "../util";
import {
    CREATE_PIPELINE,
    DELETE_PIPELINE_FAILURE,
    DELETE_PIPELINE_SUCCESS,
    EDIT_PIPELINE_FAILURE,
    EDIT_PIPELINE_SUCCESS,
    LOAD_ALL_PIPELINE_INSTANCES_SUCCESS,
    LOAD_FILTER_DESCRIPTORS_SUCCESS, LOAD_PIPELINEBLUEPRINTS_SUCCESS,
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
import {PipelineTableModel} from "./PipelineTableModel";
import {Health} from "../models/common/Health";
import {TextValue} from "../models/dataset/Value";
import {Label} from "../models/common/MetaData";

export class PipelinesState {
    public editingPipeline: EditingPipelineModel;
    public descriptors: Descriptor[];
    public filterDescriptors: ResolvedFilterDescriptor[];
    public filterCategories: ResolvedCategory[];
    public pipelineInstancePolling: boolean;
    public pipelineList: PipelineTableModel[];
}

export const initialState: PipelinesState = {
    editingPipeline: null,
    descriptors: [],
    filterDescriptors: [],
    filterCategories: [],
    pipelineInstancePolling: false,
    pipelineList: []
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
        case LOAD_PIPELINEBLUEPRINTS_SUCCESS:
            return{
                ...state,
                pipelineList:action.pipelineBlueprints.map(blueprint => {
                    let name="";
                    let description = "";
                    if(blueprint.metadata && blueprint.metadata.labels){
                        let nameValue:Label;
                        if(nameValue = blueprint.metadata.labels.find(label => label.name === 'pipeline.name')){
                            name = (nameValue.value as TextValue).value;
                        }
                        let descriptionValue:Label;
                        if(descriptionValue = (blueprint.metadata.labels.find(label => label.name === 'pipeline.description'))){
                            description = (descriptionValue.value as TextValue).value;
                        }
                    }

                    return {
                        uuid:blueprint.ref.uuid,
                        health:Health.Unknown,
                        name:name,
                        description:description
                    }
                })
            };
        case LOAD_ALL_PIPELINE_INSTANCES_SUCCESS:
            let pipelineListCopy:PipelineTableModel[] = deepcopy(state.pipelineList,[]);
            console.log("LISTCOPY: ",pipelineListCopy);
            action.pipelineInstances.forEach(instance => {
                const index = pipelineListCopy.findIndex(dataModel => dataModel.uuid === instance.id);
                if(index >= 0) {
                    pipelineListCopy[index].health = instance.health;
                }
            });
            return{
                ...state,
                pipelineList:pipelineListCopy
            };
        case DELETE_PIPELINE_SUCCESS:
            let pipelineList = deepcopy(state.pipelineList, []).filter((pipeline) => action.id !== pipeline.id);
            return {...state, pipelineList: pipelineList};
        case DELETE_PIPELINE_FAILURE:
            if (action.cause.status === 404) {
                let pipelineList = deepcopy(state.pipelineList, []).filter((pipeline) => action.id !== pipeline.id);
                return {...state, pipelineList: pipelineList};
            }
            return state;
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

export const selectPipelineList = createSelector(getPipelinesState, (state: PipelinesState) => state.pipelineList);
