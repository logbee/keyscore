import {EXTRACT_FROM_SELECTED_BLOCK_SUCCESS, PreviewActions, RESET_PREVIEW_STATE} from "../actions/preview.actions";
import {Dataset} from "../../models/dataset/Dataset";

export class PreviewState {
    outputDatasets: Dataset[];
}
export const initalPreviewState: PreviewState = {
    outputDatasets: []
};



export function PreviewReducer(state: PreviewState = initalPreviewState, action: PreviewActions): PreviewState {
    const result: PreviewState = Object.assign({}, state);

    switch (action.type) {
        case EXTRACT_FROM_SELECTED_BLOCK_SUCCESS:
            result.outputDatasets = action.output;
            return result;
        case RESET_PREVIEW_STATE:
            return Object.assign({}, initalPreviewState);
        default:
            return result;
    }
}

