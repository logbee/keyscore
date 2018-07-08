import {ActionReducerMap} from "@ngrx/store";
import {AppState} from "./app.component";
import {AppConfigReducer} from "./app.config";
import * as fromLoading from "./common/loading/loading.reducer";
import {SettingsReducer} from "./settings/settings.reducer";

export const reducers: ActionReducerMap<AppState> = {
    config: AppConfigReducer,
    settings: SettingsReducer,
    spinner: fromLoading.reducer
};
