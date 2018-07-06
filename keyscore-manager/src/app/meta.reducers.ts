import {ActionReducer, MetaReducer} from "@ngrx/store";
import {AppState} from "./app.component";

export function logger(reducer: ActionReducer<AppState>): ActionReducer<AppState> {
    return (state: AppState, action: any): AppState => {
        console.log("action", action);
        return reducer(state, action);
    };
}

export const metaReducers: Array<MetaReducer<any>> = [logger];
