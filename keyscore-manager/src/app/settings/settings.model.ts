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
    type: string;
    name: string;
    title: string;
    description: string;
    value: any;
}

export const TEXT_ITEM = "[SettingsItem] Text";
export const TEXT_CHOICE_ITEM = "[SettingsItem] TextChoice";
export const BOOLEAN_ITEM = "[SettingsItem] Boolean";

export class TextItem implements SettingsItem {
    public readonly type = TEXT_ITEM;
    constructor(
        readonly name: string,
        readonly title: string,
        readonly description: string,
        readonly value: string) { }
}

export class BooleanItem implements SettingsItem {
    public readonly type = BOOLEAN_ITEM;
    constructor(
        readonly name: string,
        readonly title: string,
        readonly description: string,
        readonly value: boolean) { }
}

export class ChoiceItem {
    public readonly type = TEXT_CHOICE_ITEM;
    constructor(readonly value: string) { }
}

export const getSettings = (state: AppState) => state.settings.current;
