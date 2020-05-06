import * as fromPipeline from "./pipelines.reducer";
import {PipelinesState} from "./pipelines.reducer";
import * as fromPreview from "./preview.reducer";
import {PreviewState} from "./preview.reducer";
import {ActionReducerMap, createFeatureSelector, createSelector} from "@ngrx/store";
import {PipelinesEffects} from "../effects/pipelines.effects";
import {PreviewEffects} from "../effects/preview.effects";

export const effects = [
    PipelinesEffects,
    PreviewEffects
];

export interface State {
    pipeline: fromPipeline.PipelinesState;
    preview: fromPreview.PreviewState;
}

export const reducers: ActionReducerMap<State> = {
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
export const getOutputDatasetMap = createSelector(selectPreviewState, (state: PreviewState) => state.outputDatasetsMap);
export const getInputDatasetMap = createSelector(selectPreviewState, (state: PreviewState) => state.inputDatasetsMap);
export const getSelectedBlock = createSelector(selectPreviewState, (state: PreviewState) => state.selectedBlock);
export const getIsLoadingDatasetsAfter = createSelector(selectPreviewState, (state: PreviewState) => state.isLoadingDatasetsAfter);
export const getIsLoadingDatasetsBefore = createSelector(selectPreviewState, (state: PreviewState) => state.isLoadingDatasetsBefore);
export const getLoadingErrorAfter = createSelector(selectPreviewState, (state: PreviewState) => state.loadingErrorAfter);
export const getLoadingErrorBefore = createSelector(selectPreviewState, (state: PreviewState) => state.loadingErrorBefore);
export const isPreviewVisible = createSelector(selectPreviewState, (state: PreviewState) => state.previewVisible);

// Pipeline Selectors
export const getPipelineList = createSelector(selectPipelineState, (state: PipelinesState) => state.pipelineList);
export const getEditingPipeline = createSelector(selectPipelineState, (state: PipelinesState) => state.editingPipeline);
export const getFilterDescriptors = createSelector(selectPipelineState, (state: PipelinesState) => state.filterDescriptors);
export const getFilterCategories = createSelector(selectPipelineState, (state: PipelinesState) => state.filterCategories);
export const getPipelinePolling = createSelector(selectPipelineState, (state: PipelinesState) => state.pipelineInstancePolling);
export const selectIsCreating = createSelector(selectPipelineState, (state: PipelinesState) => state.isPipelineCreation);