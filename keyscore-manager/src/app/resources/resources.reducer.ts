import {
    LOAD_ALL_BLUEPRINTS_SUCCESS,
    ResourcesActions
} from "./resources.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {Blueprint} from "../models/blueprints/Blueprint";

export class ResourceViewerState {
    public blueprints: Blueprint[]
}

const initialState: ResourceViewerState = {
    blueprints: []
};

export function ResourcesReducer(state: ResourceViewerState = initialState, action: ResourcesActions): ResourceViewerState {
    const result: ResourceViewerState = Object.assign({}, state);
    switch (action.type) {
        case LOAD_ALL_BLUEPRINTS_SUCCESS:
            result.blueprints = action.blueprints;
            break
    }
    return result;
}

// Selectors


export const getResourceViewerState = createFeatureSelector<ResourceViewerState>(
    "resource-viewer"
);

export const selectBlueprints = createSelector(getResourceViewerState, (state: ResourceViewerState) => state.blueprints);

