import {AppState} from "./app.component";
import {ActionReducerMap} from "@ngrx/store";
import {filterDescriptorReducer} from "./filters/filter.reducer";
import {AppConfigReducer} from "./app.config";

export const reducers: ActionReducerMap<AppState> = {
    config: AppConfigReducer,
    filterDescriptors: filterDescriptorReducer
};
