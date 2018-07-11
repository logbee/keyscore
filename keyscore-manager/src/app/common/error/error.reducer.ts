import {ERROR_EVENT, ErrorActions} from "./error.actions";
import {createFeatureSelector} from "@ngrx/store";

export interface ErrorState {
    httpError: string;
    message: string;
}

const initialState: ErrorState = {
    httpError: "",
    message: ""
};

export function ErrorReducer(state: ErrorState = initialState, action: ErrorActions): ErrorState {

    const result: ErrorState = Object.assign({}, state);

    switch (action.type) {
        case ERROR_EVENT:
            console.log("Set ErrorState " + action.httpError + " and " + action.message);
            result.httpError = action.httpError;
            result.message = action.message;
            break;
        default:
            return result;
    }
    return result
}

export const errorState = createFeatureSelector<ErrorState>(
    "error"
);

