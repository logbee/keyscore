import {ActionReducerMap} from "@ngrx/store";
import {AppState} from "./app.component";
import {AppConfigReducer} from "./app.config";
import * as fromLoading from "./loading/loading.reducer";

export const reducers: ActionReducerMap<AppState> = {
    config: AppConfigReducer,
    spinner: fromLoading.reducer
};
