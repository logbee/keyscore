import {Action} from "@ngrx/store";

export const SPINNER_SHOW = "[Loading]SpinnerShow";
export const SPINNER_HIDE = "[Loading]SpinnerHide";
export const UPDATE_REFRESH_TIME = "[Loading]UpdateRefreshTime";

export class HideSpinner implements Action {
    readonly type = SPINNER_HIDE
}

export class ShowSpinner implements Action {
    readonly type
        = SPINNER_SHOW;
}

export class UpdateRefreshTimeAction implements Action{
    readonly type = UPDATE_REFRESH_TIME;
    constructor(readonly newRefreshTime:number,readonly oldRefreshTime:number){

    }
}

export type LoadingAction = ShowSpinner | HideSpinner | UpdateRefreshTimeAction;

