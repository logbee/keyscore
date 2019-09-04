import {
    CREATE_PIPELINE,
    CREATED_PIPELINE,
    EDIT_PIPELINE_SUCCESS,
    LOAD_ALL_PIPELINE_INSTANCES_SUCCESS,
    LOAD_FILTER_DESCRIPTORS_SUCCESS,
    LOAD_PIPELINEBLUEPRINTS_SUCCESS,
    PipelineActions,
    RESOLVE_FILTER_DESCRIPTORS_SUCCESS,
    STOP_PIPELINE, STOP_PIPELINE_FAILURE,
    STOP_PIPELINE_SUCCESS,
    UPDATE_PIPELINE_POLLING,
    UPDATE_PIPELINE_SUCCESS,
} from "../actions/pipelines.actions";
import {
    Descriptor,
    EditingPipelineModel,
    generateEmptyEditingPipelineModel,
    Health,
    Label,
    Category,
    FilterDescriptor,
    TextValue
} from "@keyscore-manager-models";
import {PipelineTableModel} from "../PipelineTableModel";
import * as _ from 'lodash';

export class PipelinesState {
    public editingPipeline: EditingPipelineModel;
    public isPipelineCreation: boolean;
    public descriptors: Descriptor[];
    public filterDescriptors: FilterDescriptor[];
    public filterCategories: Category[];
    public pipelineInstancePolling: boolean;
    public pipelineList: PipelineTableModel[];
}

export const initialState: PipelinesState = {
    editingPipeline: null,
    isPipelineCreation: false,
    descriptors: [],
    filterDescriptors: [],
    filterCategories: [],
    pipelineInstancePolling: false,
    pipelineList: [],
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
            return {
                ...state,
                isPipelineCreation: true,
                editingPipeline: generateEmptyEditingPipelineModel({uuid: action.id})
            };
        case CREATED_PIPELINE:
            return {...state, isPipelineCreation: false};
        case EDIT_PIPELINE_SUCCESS:
            let editingPipeline = {
                pipelineBlueprint: action.pipelineBlueprint,
                blueprints: action.blueprints,
                configurations: action.configurations
            };
            return {...state, editingPipeline: editingPipeline};
        case UPDATE_PIPELINE_SUCCESS:
            return {
                ...state,
                editingPipeline: action.pipeline
            };
        case LOAD_PIPELINEBLUEPRINTS_SUCCESS:
            return {
                ...state,
                pipelineList: action.pipelineBlueprints.map(blueprint => {
                    let name = "";
                    let description = "";
                    if (blueprint.metadata && blueprint.metadata.labels) {
                        let nameValue: Label;
                        if (nameValue = blueprint.metadata.labels.find(label => label.name === 'pipeline.name')) {
                            name = (nameValue.value as TextValue).value;
                        }
                        let descriptionValue: Label;
                        if (descriptionValue = (blueprint.metadata.labels.find(label => label.name === 'pipeline.description'))) {
                            description = (descriptionValue.value as TextValue).value;
                        }
                    }
                    let health = Health.Unknown;
                    let index;
                    if ((index = state.pipelineList.findIndex(tableModel => tableModel.uuid === blueprint.ref.uuid)) >= 0) {
                        health = state.pipelineList[index].health;
                    }
                    return {
                        uuid: blueprint.ref.uuid,
                        health: health,
                        name: name,
                        description: description
                    }
                })
            };
        case LOAD_ALL_PIPELINE_INSTANCES_SUCCESS:
            let pipelineListCopy: PipelineTableModel[] = _.cloneDeep(state.pipelineList);
            action.pipelineInstances.forEach(instance => {
                const index = pipelineListCopy.findIndex(dataModel => dataModel.uuid === instance.id);
                if (index >= 0) {
                    pipelineListCopy[index].health = instance.health;
                }
            });
            return {
                ...state,
                pipelineList: pipelineListCopy
            };
        case STOP_PIPELINE_SUCCESS:
            let pipelineList = _.cloneDeep(state.pipelineList).filter((pipeline) => action.id !== pipeline.uuid);
            return {...state, pipelineList: pipelineList};
        case STOP_PIPELINE_FAILURE:
            if (action.cause.status === 404) {
                let pipelineList = _.cloneDeep(state.pipelineList).filter((pipeline) => action.id !== pipeline.uuid);
                return {...state, pipelineList: pipelineList};
            }
            return state;
        case UPDATE_PIPELINE_POLLING:
            return {...state, pipelineInstancePolling: action.isPolling};
        default:
            return state;
    }
}

