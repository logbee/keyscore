import {ActionReducerMap} from "@ngrx/store";
import {StreamModel, StreamsState} from "./streams.model";
import {CREATE_NEW_STREAM, StreamActions} from "./streams.actions";

export const streamsReducers: ActionReducerMap<StreamsState> = {
    streamList: StreamListReducer
};

export function StreamListReducer(state: Array<StreamModel> = [], action: StreamActions): Array<StreamModel> {

    const result: Array<StreamModel> = new Array<StreamModel>();
    state.forEach(model => result.push(model));

    console.log('result:', result);
    switch (action.type){
        case CREATE_NEW_STREAM:
            result.push(new StreamModel(action.id, action.name, action.description));
    }

    return result
}
