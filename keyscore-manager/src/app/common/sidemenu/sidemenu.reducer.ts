import {MenuAction, TOGGLE_MENU} from "./sidemenu.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";

export interface MenuState {
    isExpanded: boolean;
}

const initialState: MenuState = {
    isExpanded: true
};

export function MenuReducer(state: MenuState = initialState, action: MenuAction): MenuState {
    switch (action.type) {
        case TOGGLE_MENU:
            return {...state, isExpanded: !state.isExpanded};
            break;
        default:
            return state;
    }
}

export const selectMenuEntity = createFeatureSelector<MenuState>("menu");
export const isMenuExpanded = createSelector(selectMenuEntity, (state: MenuState) => state.isExpanded);
