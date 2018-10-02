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

export class ResourceViewerState {
    public blueprints: Blueprint[];
    public descriptors: ResolvedFilterDescriptor[];
    public configurations: Configuration[];
    public configurationRef: string;
    public descriptorRef: string;
    public blueprintRef: string;
    public stateObjects: StateObject[]
}

const initialState: ResourceViewerState = {
    blueprints: [],
    descriptors: [],
    configurations: [],
    configurationRef: "",
    descriptorRef: "",
    blueprintRef: "",
    stateObjects: []

};

export function ResourcesReducer(state: ResourceViewerState = initialState, action: ResourcesActions): ResourceViewerState {
    const result: ResourceViewerState = Object.assign({}, state);
    switch (action.type) {
        case LOAD_ALL_BLUEPRINTS_SUCCESS:
            result.blueprints = action.blueprints;
            break;
        case RESOLVED_ALL_DESCRIPTORS_SUCCESS:
            result.descriptors = action.resolvedDescriptors;
            break;
        case LOAD_CONFIGURATIONS_SUCCESS:
            result.configurations = action.configurations;
            break;
        case STORE_CONFIGURATION_REF:
            result.configurationRef = action.uuid;
            break;
        case STORE_BLUEPRINT_REF:
            result.blueprintRef = action.uuid;
            break;
        case STORE_DESCRIPTOR_REF:
            result.descriptorRef = action.uuid;
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
export const selectDescriptor = createSelector(getResourceViewerState, (state: ResourceViewerState) => state.descriptors.filter((desc) => desc.descriptorRef.uuid == state.descriptorRef)[0]);
export const selectConfiguration = createSelector(getResourceViewerState, (state: ResourceViewerState) => state.configurations.filter((config) => config.ref.uuid == state.configurationRef)[0]);
export const selectStateObjects = createSelector(getResourceViewerState, (state: ResourceViewerState) => state.stateObjects);
