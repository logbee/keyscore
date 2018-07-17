import {Action} from "@ngrx/store";
import {SettingsItem, SettingsState} from "./settings.model";

export const LOAD_SETTINGS = "[Settings] Load";
export const LOAD_SETTINGS_SUCCESS = "[Settings] LoadSuccess";
export const LOAD_SETTINGS_FAILURE = "[Settings] LoadFailure";
export const SAVE_SETTINGS = "[Settings] Save";
export const SAVE_SETTINGS_SUCCESS = "[Settings] SaveSuccess";
export const SAVE_SETTINGS_FAILURE = "[Settings] SaveFailure";
export const APPLY_SETTINGS = "[Settings] Apply";
export const UPDATE_SETTINGS_ITEM = "[Settings] UpdateItem";

export type SettingsActions =
    | LoadSettingsAction
    | LoadSettingsSuccessAction
    | LoadSettingsFailureAction
    | SaveSettingsAction
    | SaveSettingsSuccessAction
    | SaveSettingsFailureAction
    | ApplySettingsAction
    | UpdateSettingsItemAction;

export class LoadSettingsAction implements Action {
    public readonly type = LOAD_SETTINGS;
}

export class LoadSettingsSuccessAction implements Action {
    public readonly type = LOAD_SETTINGS_SUCCESS;
    constructor(readonly settings: SettingsState) { }
}

export class LoadSettingsFailureAction implements Action {
    public readonly type = LOAD_SETTINGS_FAILURE;
    constructor(readonly cause: any) { }
}

export class SaveSettingsAction implements Action {
    public readonly type = SAVE_SETTINGS;
    constructor(readonly settings: SettingsState) { }
}

export class SaveSettingsSuccessAction implements Action {
    public readonly type = SAVE_SETTINGS_SUCCESS;
    constructor(readonly settings: SettingsState) { }
}

export class SaveSettingsFailureAction implements Action {
    public readonly type = SAVE_SETTINGS_FAILURE;
    constructor(readonly cause: any) { }
}

export class ApplySettingsAction implements Action  {
    public readonly type = APPLY_SETTINGS;
}

export class UpdateSettingsItemAction implements Action  {
    public readonly type = UPDATE_SETTINGS_ITEM;
    constructor(readonly item: SettingsItem) { }
}