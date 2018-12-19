import {PreviewActions} from "../actions/preview.actions";

export class PreviewState {
    public paused: number;
}
export const initalPreviewState: PreviewState = {
    paused: 0
};



export function PreviewReducer(state: PreviewState = initalPreviewState, action: PreviewActions): PreviewState {
    const result: PreviewState = Object.assign({}, state);

    switch (action.type) {
        default:
            return result;
    }
}

