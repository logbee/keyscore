import {Action} from "@ngrx/store";

export const TOGGLE_MENU = "[Sidemenu] ToggleMenu";

export class ToggleMenuAction implements Action {
    public readonly type = TOGGLE_MENU;
}

export type MenuAction = ToggleMenuAction;
