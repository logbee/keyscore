import {
    GET_RESOURCE_STATE_SUCCESS,
    LOAD_ALL_BLUEPRINTS_SUCCESS,
    LOAD_CONFIGURATIONS_SUCCESS,
    RESOLVED_ALL_DESCRIPTORS_SUCCESS,
    ResourcesActions,
    STORE_BLUEPRINT_REF,
    STORE_CONFIGURATION_REF,
    STORE_DESCRIPTOR_REF
} from "./resources.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {Blueprint} from "../models/blueprints/Blueprint";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {Configuration} from "../models/common/Configuration";
import {StateObject} from "../models/common/StateObject";
import {deepcopy} from "../util";
import {ResourceTableModel} from "../models/resources/ResourceTableModel";
import * as _ from "lodash";

export class ResourceViewerState {
    public blueprints: Blueprint[];
    public descriptors: ResolvedFilterDescriptor[];
    public configurations: Configuration[];
    public stateObjects: StateObject[];
    public resourceModels: ResourceTableModel[];
}

const initialState: ResourceViewerState = {
    blueprints: [],
    descriptors: [],
    configurations: [],
    stateObjects: [],
    resourceModels: []
};

export function ResourcesReducer(state: ResourceViewerState = initialState, action: ResourcesActions): ResourceViewerState {
    let result = _.cloneDeep(state);
    switch (action.type) {
        case LOAD_ALL_BLUEPRINTS_SUCCESS:
            result.blueprints = action.blueprints;
            const models: ResourceTableModel [] = [];
            result.blueprints.forEach(bp => {
                models.push(new ResourceTableModel(bp, undefined, undefined));
            });
            result.resourceModels = models;
            break;
        case RESOLVED_ALL_DESCRIPTORS_SUCCESS:
            result.descriptors = action.resolvedDescriptors;
            let tmpModels = result.resourceModels;
            result.descriptors.forEach(descriptor => {
                tmpModels.forEach(model => {
                    if (descriptor.descriptorRef.uuid === model.blueprint.descriptor.uuid) {
                        model.descriptor = descriptor;
                    }
                });
            });
            result.resourceModels = tmpModels;
            break;
        case LOAD_CONFIGURATIONS_SUCCESS:
            result.configurations = action.configurations;
            let tmp = result.resourceModels;
            result.configurations.forEach(config => {
                tmp.forEach(model => {
                    if (config.ref.uuid === model.blueprint.configuration.uuid) {
                        model.configuration = config;
                    }
                });
            });
            result.resourceModels = tmp;
            break;
        case GET_RESOURCE_STATE_SUCCESS:
            let copy = deepcopy(result.stateObjects, []);
            copy.push(new StateObject(action.resourceId, action.instance));
            result.stateObjects = copy;
            break;
    }
    return result;
}

// Selectors
export const getResourceViewerState = createFeatureSelector<ResourceViewerState>(
    "resource-viewer"
);

export const selectBlueprints = createSelector(getResourceViewerState, (state: ResourceViewerState) => state.blueprints);
export const selectStateObjects = createSelector(getResourceViewerState, (state: ResourceViewerState) => state.stateObjects);
export const selectConfigurations = createSelector(getResourceViewerState, (state: ResourceViewerState) => state.configurations);
export const selectDescriptors = createSelector(getResourceViewerState, (state: ResourceViewerState) => state.descriptors);
export const selectTableModels = createSelector(getResourceViewerState, (state: ResourceViewerState) => state.resourceModels);