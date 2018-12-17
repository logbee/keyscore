import {DataPreviewActions, TRIGGER_DATA_PREVIEW} from "./data-preview.actions";

export class DataPreviewState {
    public blockIds: string[];
}

export const initialState: DataPreviewState = {
    blockIds: []
};

export function DataPreviewReducer(state: DataPreviewState = initialState, action: DataPreviewActions): DataPreviewState {
    switch (action.type) {
        case TRIGGER_DATA_PREVIEW:
            console.log("PREVIEW: Triggered Data Preview Reducer Case");
            break;
        default:
            return state;
    }
}