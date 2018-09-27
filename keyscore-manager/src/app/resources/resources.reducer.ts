import {
    LOAD_ALL_BLUEPRINTS_SUCCESS, LOAD_CONFIGURATIONS_SUCCESS, RESOLVED_ALL_DESCRIPTORS_SUCCESS,
    ResourcesActions
} from "./resources.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {Blueprint} from "../models/blueprints/Blueprint";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {Configuration} from "../models/common/Configuration";

export class ResourceViewerState {
    public blueprints: Blueprint[];
    public descriptors: ResolvedFilterDescriptor[];
    public configurations: Configuration[];
}

const initialState: ResourceViewerState = {
    blueprints: [],
    descriptors: [],
    configurations: []
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
    }
    return result;
}

// Selectors
export const getResourceViewerState = createFeatureSelector<ResourceViewerState>(
    "resource-viewer"
);

export const selectBlueprints = createSelector(getResourceViewerState, (state: ResourceViewerState) => state.blueprints);

