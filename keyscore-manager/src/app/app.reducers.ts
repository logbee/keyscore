import {ActionReducerMap} from "@ngrx/store";
import {AppState} from "./app.component";
import {AppConfigReducer} from "./app.config";
import {SettingsReducer} from "./settings/settings.reducer";
import {MenuReducer} from "./common/sidemenu/sidemenu.reducer";
import {LoadingReducer} from "./common/loading/loading.reducer";
import {ErrorReducer} from "./common/error/error.reducer";
import {SnackbarReducer} from "./common/snackbar/snackbar.reducer";

export const reducers: ActionReducerMap<AppState> = {
    config: AppConfigReducer,
    settings: SettingsReducer,
    spinner: LoadingReducer,
    menu: MenuReducer,
    error: ErrorReducer,
    snackbar: SnackbarReducer
};
