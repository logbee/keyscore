import {StreamsState} from "./streams.model";
import {CREATE_STREAM, DELETE_STREAM, EDIT_STREAM, RESET_STREAM, StreamActions, UPDATE_STREAM} from "./streams.actions";

const initialState: StreamsState = {
    streamList: [],
    editingStream: null
};

export function StreamsReducer(state: StreamsState = initialState, action: StreamActions): StreamsState {

    const result: StreamsState = Object.assign({}, state);

    switch (action.type){
        case CREATE_STREAM:
            result.streamList.push({
                id: action.id,
                name: action.name,
                description: action.description,
                filters: []
            });
            break;
        case EDIT_STREAM:
            setEditingStream(result, action.id);
            break;
        case RESET_STREAM:
            setEditingStream(result, action.id);
            break;
        case UPDATE_STREAM:
            const index = result.streamList.findIndex(stream => action.stream.id == stream.id);
            if (index >= 0) {
                result.streamList[index] = action.stream;
            }
            if (result.editingStream != null && result.editingStream.id == action.stream.id) {
                result.editingStream = action.stream;
            }
            break;
        case DELETE_STREAM:
            result.streamList = result.streamList.filter(stream => action.id != stream.id);
            break;
    }

    return result
}

function setEditingStream(state: StreamsState, id: string) {
    state.editingStream = Object.assign({}, state.streamList.find(stream => id == stream.id));
}