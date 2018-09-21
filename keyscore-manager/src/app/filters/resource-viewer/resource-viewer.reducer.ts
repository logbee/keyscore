import {
    LOAD_ALL_BLUEPRINTS_SUCCESS,
    ResourceViewerActions
} from "./resource-viewer.actions";
import {Blueprint} from "../../models/blueprints/Blueprint";
import {createFeatureSelector, createSelector} from "@ngrx/store";

export class ResourceViewerState {
    public blueprints: Blueprint[]
}

const initialState: ResourceViewerState = {
    blueprints: []
};

export function ResourceViewerReducer(state: ResourceViewerState = initialState, action: ResourceViewerActions): ResourceViewerState {
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

