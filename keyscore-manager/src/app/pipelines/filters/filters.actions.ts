import {Action} from "@ngrx/store";
import {FilterConfiguration} from "../pipelines.model";

export const LOAD_LIVE_EDITING_FILTER = "[Filter] LoadLiveEditingFilter";
export const LOAD_LIVE_EDITING_FILTER_FAILURE = "[Filter] LoadLiveEditingFilterFailure";
export const LOAD_LIVE_EDITING_FILTER_SUCCESS = "[Filter] LoadLiveEditingFilterSuccess";
export type FiltersActions =

    | LoadLiveEditingFilterAction
    | LoadLiveEditingFilterFailure
    | LoadLiveEditingFilterSuccess;

export class LoadLiveEditingFilterAction implements Action {
    public readonly type = LOAD_LIVE_EDITING_FILTER;

    constructor(readonly filterId: string) {
    }
}

export class LoadLiveEditingFilterSuccess implements Action {
    public readonly type = LOAD_LIVE_EDITING_FILTER_SUCCESS;

    constructor(readonly filter: FilterConfiguration) {
    }
}

export class LoadLiveEditingFilterFailure implements Action {
    public readonly type = LOAD_LIVE_EDITING_FILTER_FAILURE;

    constructor(readonly cause: any) {
    }
}
