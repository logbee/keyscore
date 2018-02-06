import {Action, ActionReducerMap, createFeatureSelector, createSelector} from "@ngrx/store";

export class StreamsState {
    streamList: StreamModel[];

}

export const getStreamsState = createFeatureSelector<StreamsState>('streams');

export const getStreamList = createSelector(getStreamsState, (state: StreamsState) => state.streamList);

export class StreamModel {
    constructor(readonly id: string, readonly name: string, readonly description: string) {

    }
}

export const CREATE_NEW_STREAM = '[Stream] CreateNewStream';

export class CreateNewStreamAction implements Action {
    readonly type = '[Stream] CreateNewStream'

    constructor(readonly id: string, readonly name: string, readonly description: string) {
    }
}

export type StreamActions =
    | CreateNewStreamAction

export function StreamListReducer(state: StreamModel[], action: StreamActions): StreamModel[] {

    const result: StreamModel[] = Object.assign({}, state);

    switch (action.type){
        case CREATE_NEW_STREAM:
            result.push(new StreamModel(action.id, action.name, action.description));
    }

    return result
}


export const streamsReducers: ActionReducerMap<StreamsState> = {
    streamList: StreamListReducer
};