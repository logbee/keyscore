import * as fromPipeline from "./reducers/pipelines.reducer";
import * as fromPreview from "./reducers/preview.reducer";
import {createFeatureSelector, createSelector} from "@ngrx/store";
import {PreviewState} from "./reducers/preview.reducer";
import {PipelinesState} from "./reducers/pipelines.reducer";

export interface State {
    pipeline: fromPipeline.PipelinesState;
    preview: fromPreview.PreviewState;
}

export const reducers = {
    pipeline: fromPipeline.PipelinesReducer,
    preview: fromPreview.PreviewReducer,
};

export const selectState = createFeatureSelector<State>('state');

export const selectPreviewState = createSelector(
    selectState, (state: State) => state.preview
);

export const selectPipelineState = createSelector(
    selectState, (state: State) => state.pipeline
);

// PreviewSelectors
export const getTriggeredFlag = createSelector(selectPreviewState, (state: PreviewState) => state.triggered);

// Pipeline Selectors
export const getPipelineList = createSelector(selectPipelineState, (state: PipelinesState) => state.pipelineList);
export const getEditingPipeline = createSelector(selectPipelineState, (state: PipelinesState) => state.editingPipeline);
export const getFilterDescriptors = createSelector(selectPipelineState, (state: PipelinesState) => state.filterDescriptors);
export const getFilterCategories = createSelector(selectPipelineState, (state: PipelinesState) => state.filterCategories);
export const getPipelinePolling = createSelector(selectPipelineState, (state: PipelinesState) => state.pipelineInstancePolling);