import {ERROR_ACTION, ERROR_RESET_ACTION, ErrorActions} from "./error.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";

export interface ErrorState {
    httpError: string;
    message: string;
    isError: boolean;
}

const initialState: ErrorState = {
    httpError: "",
    message: "",
    isError: false
};

export function ErrorReducer(state: ErrorState = initialState, action: ErrorActions): ErrorState {

    switch (action.type) {
        case ERROR_ACTION:
            return {httpError: action.httpError, message: action.message,isError: true};
        case ERROR_RESET_ACTION:
            return {...state, isError: false};
        default:
            return state;
    }
}

export const errorState = createFeatureSelector<ErrorState>(
    "error"
);
export const isError = createSelector(errorState, (state: ErrorState) => state.isError);
export const selectErrorMessage = createSelector(errorState, (state: ErrorState) => state.message);
export const selectHttpErrorCode = createSelector(errorState, (state: ErrorState) => state.httpError);

