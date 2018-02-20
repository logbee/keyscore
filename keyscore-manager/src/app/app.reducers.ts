import {AppState} from "./app.component";
import {ActionReducerMap} from "@ngrx/store";
import {AppConfigReducer} from "./app.config";

export const reducers: ActionReducerMap<AppState> = {
    config: AppConfigReducer,
};
