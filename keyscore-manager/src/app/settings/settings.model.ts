import {AppState} from "../app.component";

export interface SettingsState {
    current: SettingsModel;
    default: SettingsModel;
}

export interface SettingsModel {
    groups: SettingsGroup[];
}

export interface SettingsGroup {
    name: string;
    title: string;
    items: SettingsItem[];
}

export interface SettingsItem {
    name: string;
    value: any;
}

export const getSettings = (state: AppState) => state.settings.current;
