import {createFeatureSelector, createSelector} from "@ngrx/store";

export class StreamsState {
    streamList: Array<StreamModel>;

}

export class StreamModel {
    constructor(readonly id: string, readonly name: string, readonly description: string) {

    }
}

export const getStreamsState = createFeatureSelector<StreamsState>('streams');

export const getStreamList = createSelector(getStreamsState, (state: StreamsState) => state.streamList);
