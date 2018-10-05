import {SNACKBAR_CLOSE, SNACKBAR_OPEN, SnackbarAction} from "./snackbar.actions";

export interface SnackbarState {
    show: boolean;
}

const initialState: SnackbarState = {
    show: false
};

export function SnackbarReducer(state: SnackbarState = initialState, action: SnackbarAction) {
    switch(action.type) {
        case SNACKBAR_CLOSE:
            return { ...state, show: false };
        case SNACKBAR_OPEN:
            return { ...state, show: true };
        default:
            return state;
    }
}