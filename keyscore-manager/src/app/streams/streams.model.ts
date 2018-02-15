import {createFeatureSelector, createSelector} from "@ngrx/store";

export class StreamsState {
    streamList: Array<StreamModel>;
    editingStream: StreamModel;
    loading: boolean;
}

export interface StreamModel {
    id: string,
    name: string,
    description: string,
    filters: Array<FilterModel>
}

export interface FilterModel {
    id: string,
    name: string,
    description: string
}

export const getStreamsState = createFeatureSelector<StreamsState>('streams');

export const getStreamList = createSelector(getStreamsState, (state: StreamsState) => state.streamList);

export const getEditingStream = createSelector(getStreamsState, (state: StreamsState) => state.editingStream);

export const isLoading = createSelector(getStreamsState, (state: StreamsState) => state.loading);