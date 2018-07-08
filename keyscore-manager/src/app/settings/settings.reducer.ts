import {BooleanItem, SettingsState, TextItem} from "./settings.model";
import {LOAD_SETTINGS_SUCCESS, SettingsActions, UPDATE_SETTINGS} from "./settings.actions";

const initialState: SettingsState = {
    current: {
        groups: [
            {
                name: "general",
                title: "SETTINGS.GROUP_GENERAL_TITLE",
                items: [
                    new TextItem(
                        "base-url",
                        "SETTINGS.ITEMS.BASE_URL_TITLE",
                        "SETTINGS.ITEMS.BASE_URL_DESCRIPTION",
                        "http://localhost:4711"),
                    new TextItem(
                        "language",
                        "SETTINGS.ITEMS.LANGUAGE_TITLE",
                        "SETTINGS.ITEMS.LANGUAGE_DESCRIPTION",
                        "http://localhost:4711")
                ]
            },
            {
                name: "features",
                title: "SETTINGS.GROUP_FEATURES_TITLE",
                items: [
                    new BooleanItem(
                        "blockly",
                        "SETTINGS.ITEMS.BLOCKLY_ENABLED_TITLE",
                        "SETTINGS.ITEMS.BLOCKLY_ENABLED_DESCRIPTION",
                        false),
                    new BooleanItem(
                        "live-editing",
                        "SETTINGS.ITEMS.LIVEEDITING_ENABLED_TITLE",
                        "SETTINGS.ITEMS.LIVEEDITING_ENABLED_DESCRIPTION",
                        false),
                    new BooleanItem(
                        "settings",
                        "SETTINGS.ITEMS.SETTINGS_ENABLED_TITLE",
                        "SETTINGS.ITEMS.SETTINGS_ENABLED_DESCRIPTION",
                        false)
                ]
            }
        ]
    },
    default: null
};

export function SettingsReducer(state: SettingsState = initialState, action: SettingsActions): SettingsState {

    const result: SettingsState = Object.assign({}, state);

    switch (action.type) {
        case LOAD_SETTINGS_SUCCESS:
        case UPDATE_SETTINGS:
        default:
    }

    return result;
}
