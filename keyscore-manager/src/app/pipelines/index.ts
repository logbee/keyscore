import * as fromPipeline from "./reducers/pipelines.reducer";
import {PipelinesState} from "./reducers/pipelines.reducer";
import * as fromPreview from "./reducers/preview.reducer";
import {PreviewState} from "./reducers/preview.reducer";
import {createFeatureSelector, createSelector, Selector} from "@ngrx/store";
import {PipelinesEffects} from "./effects/pipelines.effects";
import {PreviewEffects} from "./effects/preview.effects";
import { Dataset } from "@keyscore-manager-models/src/main/dataset/Dataset";

export const effects = [
    PipelinesEffects,
    PreviewEffects
];

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
export const getOutputDatasetMap: Selector<PreviewState, Map<string, Dataset[]>> = createSelector(selectPreviewState, (state: PreviewState) => state.outputDatasetsMap);
export const getInputDatasetMap: Selector<PreviewState, Map<string, Dataset[]>> = createSelector(selectPreviewState, (state: PreviewState) => state.inputDatasetsMap);
export const getSelectedBlock: Selector<PreviewState, string> = createSelector(selectPreviewState, (state: PreviewState) => state.selectedBlock);


// Pipeline Selectors
export const getPipelineList = createSelector(selectPipelineState, (state: PipelinesState) => state.pipelineList);
export const getEditingPipeline = createSelector(selectPipelineState, (state: PipelinesState) => state.editingPipeline);
export const getFilterDescriptors = createSelector(selectPipelineState, (state: PipelinesState) => state.filterDescriptors);
export const getFilterCategories = createSelector(selectPipelineState, (state: PipelinesState) => state.filterCategories);
export const getPipelinePolling = createSelector(selectPipelineState, (state: PipelinesState) => state.pipelineInstancePolling);
export const selectIsCreating = createSelector(selectPipelineState, (state: PipelinesState) => state.isPipelineCreation);
