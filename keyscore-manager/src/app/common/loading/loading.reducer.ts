import {
    DECREMENT_LOADING_COUNTER,
    INCREMENT_LOADING_COUNTER,
    LoadingAction,
    SPINNER_HIDE,
    SPINNER_SHOW,
    UPDATE_REFRESH_TIME
} from "./loading.actions";
import {createFeatureSelector, createSelector} from "@ngrx/store";

export interface LoadingState {
    show: boolean;
    refreshTime: number;
    loadingActionsCounter: number;
}

const initialState: LoadingState = {
    show: false,
    refreshTime: 10000,
    loadingActionsCounter: 0
};

export function LoadingReducer(state: LoadingState = initialState, action: LoadingAction): LoadingState {
    switch (action.type) {
        case SPINNER_HIDE:
            return {...state, show: state.loadingActionsCounter > 0};
        case SPINNER_SHOW:
            return {...state, show: true};
        case UPDATE_REFRESH_TIME:
            return {...state, refreshTime: action.newRefreshTime};
        case INCREMENT_LOADING_COUNTER:
            return {...state, loadingActionsCounter: state.loadingActionsCounter + 1};
        case DECREMENT_LOADING_COUNTER:
            return {
                ...state,
                loadingActionsCounter: state.loadingActionsCounter > 0 ?
                    state.loadingActionsCounter - 1 :
                    state.loadingActionsCounter
            };
        default :
            return state;
    }
}

export const isShowing = (state: LoadingState) => state.show;

export const selectSpinnerEntity = createFeatureSelector<LoadingState>(
    "spinner"
);
export const isSpinnerShowing = createSelector(selectSpinnerEntity, isShowing);
export const selectRefreshTime = createSelector(selectSpinnerEntity, (state: LoadingState) => state.refreshTime);
