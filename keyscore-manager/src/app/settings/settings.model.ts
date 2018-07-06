import {AppState} from "../app.component";

export interface SettingsState {
    current: SettingsModel;
    default: SettingsModel;
}

export interface SettingsModel {
    groups: SettingsGroup[]
}

export interface SettingsGroup {
    name: String
    title: String
    items: SettingsItem[]
}

export interface SettingsItem {
    name: String,
    value: Object
}

export const getSettings = (state: AppState) => state.settings.current;