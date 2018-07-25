import {BooleanItem, SettingsState, TextChoiceItem, TextItem} from "./settings.model";
import {APPLY_SETTINGS, LOAD_SETTINGS_SUCCESS, SettingsActions, UPDATE_SETTINGS_ITEM} from "./settings.actions";
import {AppConfigActions, CONFIG_LOADED} from "../app.config";

const defaultSettings = {
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
                new TextChoiceItem(
                    "language",
                    "SETTINGS.ITEMS.LANGUAGE_TITLE",
                    "SETTINGS.ITEMS.LANGUAGE_DESCRIPTION",
                    new TextItem("language.en", "LANGUAGES.ENGLISH", "", "en"),
                    [
                        new TextItem("language.en", "LANGUAGES.ENGLISH", "", "en"),
                        new TextItem("language.de", "LANGUAGES.GERMAN", "", "de")
                    ])
            ]
        },
        {
            name: "features",
            title: "SETTINGS.GROUP_FEATURES_TITLE",
            items: [
                new BooleanItem(
                    "features.blockly",
                    "SETTINGS.ITEMS.BLOCKLY_ENABLED_TITLE",
                    "SETTINGS.ITEMS.BLOCKLY_ENABLED_DESCRIPTION",
                    false),
                new BooleanItem(
                    "features.live-editing",
                    "SETTINGS.ITEMS.LIVEEDITING_ENABLED_TITLE",
                    "SETTINGS.ITEMS.LIVEEDITING_ENABLED_DESCRIPTION",
                    false),
                new BooleanItem(
                    "features.settings",
                    "SETTINGS.ITEMS.SETTINGS_ENABLED_TITLE",
                    "SETTINGS.ITEMS.SETTINGS_ENABLED_DESCRIPTION",
                    false)
            ]
        }
    ]
};

const initialState: SettingsState = {
    default: defaultSettings,
    active: defaultSettings,
    modified: defaultSettings,
    isModified: false
};

export function SettingsReducer(state: SettingsState = initialState, action: SettingsActions | AppConfigActions): SettingsState {

    const LOCAL_STORAGE_KEY = "io.logbee.keyscore.settings";
    const result: SettingsState = Object.assign({}, state);

    switch (action.type) {
        case LOAD_SETTINGS_SUCCESS:
            break;

        case CONFIG_LOADED:
            // defaults to restore
//            action.payload

            result.active = JSON.parse(localStorage.getItem(LOCAL_STORAGE_KEY))
            result.modified = result.active;
            break;

        case APPLY_SETTINGS:
            result.active = state.modified;
            localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(state.active));
            result.isModified = false;
            break;

        case UPDATE_SETTINGS_ITEM:
            result.modified.groups = state.modified.groups.map(group => {
                return { name: group.name, title: group.title, items: group.items.map(item => {
                    if (item.name === action.item.name) {
                        return action.item;
                    }
                    return item;
                })};
            });
            result.isModified = true;
            break;

        default:
    }

    return result;
}
