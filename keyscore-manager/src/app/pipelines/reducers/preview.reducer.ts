import {PreviewActions, TEST_ACTION} from "../actions/preview.actions";
import {createSelector} from "@ngrx/store";
import {selectPreviewState} from "../index";


export class PreviewState {
    public triggered: string;
}
export const initalPreviewState: PreviewState = {
    triggered: "triggered"
};



export function PreviewReducer(state: PreviewState = initalPreviewState, action: PreviewActions): PreviewState {
    switch (action.type) {
        case TEST_ACTION:
            console.log("TEST ACTION triggered in PreviewReducer");
            return state;
        default:
            return state;
    }
}

