import {AppState} from "./app.component";
import {ActionReducerMap} from "@ngrx/store";
import {streamEditorReducer} from "./streams/stream-editor/stream-editor.reducer";
import {filterDescriptorReducer} from "./filters/filter.reducer";
import {AppConfigReducer} from "./app.config";

export const reducers: ActionReducerMap<AppState> = {
    config: AppConfigReducer,
    stream: streamEditorReducer,
    filterDescriptors: filterDescriptorReducer
};
