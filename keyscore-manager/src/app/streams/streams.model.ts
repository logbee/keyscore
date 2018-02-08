import {createFeatureSelector, createSelector} from "@ngrx/store";

export class StreamsState {
    streamList: Array<StreamModel>;
}

export interface StreamModel {
    id: string,
    name: string,
    description: string
    editing: boolean
}

export const getStreamsState = createFeatureSelector<StreamsState>('streams');

export const getStreamList = createSelector(getStreamsState, (state: StreamsState) => state.streamList);
