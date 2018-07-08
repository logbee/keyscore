import {Action} from "@ngrx/store";

export const SPINNER_SHOW = "[Loading]SpinnerShow";
export const SPINNER_HIDE = "[Loading]SpinnerHide";
export const UPDATE_REFRESH_TIME = "[Loading]UpdateRefreshTime";
export const INCREMENT_LOADING_COUNTER = "[Loading]IncrementLoadingCounter";
export const DECREMENT_LOADING_COUNTER = "[Loading]DecrementLoadingCounter";

export class HideSpinner implements Action {
    public readonly type = SPINNER_HIDE;
}

export class ShowSpinner implements Action {
    public readonly type
        = SPINNER_SHOW;
}

export class UpdateRefreshTimeAction implements Action {
    public readonly type = UPDATE_REFRESH_TIME;

    constructor(readonly newRefreshTime: number, readonly oldRefreshTime: number) {

    }
}

export class IncrementLoadingCounterAction implements Action {
    public readonly type = INCREMENT_LOADING_COUNTER;
}

export class DecrementLoadingCounterAction implements Action {
    public readonly type = DECREMENT_LOADING_COUNTER;
}

export type LoadingAction =
    ShowSpinner
    | HideSpinner
    | UpdateRefreshTimeAction
    | IncrementLoadingCounterAction
    | DecrementLoadingCounterAction;
