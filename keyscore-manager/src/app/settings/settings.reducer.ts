
import {SettingsState} from "./settings.model";
import {LOAD_SETTINGS_SUCCESS, SettingsActions, UPDATE_SETTINGS} from "./settings.actions";

const initialState: SettingsState = {
    current: {
        groups: [
            {
                name: "general",
                title: "SETTINGS.GROUP_GENERAL_TITLE",
                items: []
            },
            {
                name: "features",
                title: "SETTINGS.GROUP_FEATURES_TITLE",
                items: []
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
