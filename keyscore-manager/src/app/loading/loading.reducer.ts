import {SPINNER_HIDE, SPINNER_SHOW, LoadingAction, UPDATE_REFRESH_TIME} from "./loading.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";

export interface State {
    show: boolean;
    refreshTime: number;
}

const initialState: State = {
    show: false,
    refreshTime: 10000
};

export function reducer(state: State = initialState, action: LoadingAction) {
    switch (action.type) {
        case SPINNER_HIDE:
            return {...state, show: false};
        case SPINNER_SHOW:
            return {...state, show: true};
        case UPDATE_REFRESH_TIME:
            return {...state, refreshTime: action.newRefreshTime};
        default :
            return state;
    }
}

export const isShowing = (state: State) => state.show;

export const selectSpinnerEntity = createFeatureSelector<State>(
    "spinner"
);
export const isSpinnerShowing = createSelector(selectSpinnerEntity, isShowing);
export const selectRefreshTime = createSelector(selectSpinnerEntity, (state: State) => state.refreshTime);