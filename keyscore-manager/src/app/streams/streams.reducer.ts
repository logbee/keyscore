import {ActionReducerMap} from "@ngrx/store";
import {StreamModel, StreamsState} from "./streams.model";
import {CREATE_STREAM, EDIT_STREAM, StreamActions} from "./streams.actions";

export const streamsReducers: ActionReducerMap<StreamsState> = {
    streamList: StreamListReducer
};

function StreamListReducer(state: Array<StreamModel> = [], action: StreamActions): Array<StreamModel> {

    const result: Array<StreamModel> = new Array<StreamModel>();
    state.forEach(model => result.push(model));

    switch (action.type){
        case CREATE_STREAM:
            result.push({
                id: action.id,
                name: action.name,
                description: action.description,
                editing: false
            });
            break;
        case EDIT_STREAM:
            result.forEach(stream => stream.editing = action.id == stream.id);
            break;
    }

    return result
}
