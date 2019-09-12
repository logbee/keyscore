import * as fromPipeline from "./reducers/pipelines.reducer";
import {PipelinesState} from "./reducers/pipelines.reducer";
import * as fromPreview from "./reducers/preview.reducer";
import {PreviewState} from "./reducers/preview.reducer";
import {createFeatureSelector, createSelector, Selector} from "@ngrx/store";
import {PipelinesEffects} from "./effects/pipelines.effects";
import {PreviewEffects} from "./effects/preview.effects";
import {DatasetTableModel} from "@/../modules/keyscore-manager-models/src/main/dataset/DatasetTableModel";

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
export const getOutputDatasetModels: Selector<PreviewState, Map<string, DatasetTableModel[]>> = createSelector(selectPreviewState, (state: PreviewState) => state.outputDatasetModelMap);
export const getInputDatsetModels: Selector<PreviewState, Map<string, DatasetTableModel[]>> = createSelector(selectPreviewState, (state: PreviewState) => state.inputDatasetModelMap);
export const getSelectedBlock: Selector<PreviewState, string> = createSelector(selectPreviewState, (state: PreviewState) => state.selectedBlock);


// Pipeline Selectors
export const getPipelineList = createSelector(selectPipelineState, (state: PipelinesState) => state.pipelineList);
export const getEditingPipeline = createSelector(selectPipelineState, (state: PipelinesState) => state.editingPipeline);
export const getFilterDescriptors = createSelector(selectPipelineState, (state: PipelinesState) => state.filterDescriptors);
export const getFilterCategories = createSelector(selectPipelineState, (state: PipelinesState) => state.filterCategories);
export const getPipelinePolling = createSelector(selectPipelineState, (state: PipelinesState) => state.pipelineInstancePolling);
export const selectIsCreating = createSelector(selectPipelineState, (state: PipelinesState) => state.isPipelineCreation);
