import {Action} from "@ngrx/store";
import {FilterConfiguration, FilterDescriptor, PipelineConfiguration, PipelineInstance} from "../pipelines.model";

export const LOAD_LIVE_EDITING_FILTER = "[Filter] LoadLiveEditingFilterAction";
export const LOAD_LIVE_EDITING_FILTER_FAILURE = "[Filter] LoadLiveEditingFilterFailureAction";
export const SET_LIVE_EDITING_FILTER = "[Filter] SetLiveEditingFilterAction";
export const SET_LIVE_EDITING_FILTER_FAILURE = "[Filter] SetLiveEditingFilterFailureAction";

export type FiltersActions =
    | LoadLiveEditingFilterAction
    | LoadLiveEditingFilterFailureAction
    | SetLiveEditingFilterFailureAction
    | SetLiveEditingFilterAction;

export class LoadLiveEditingFilterAction implements Action {
    public readonly type = LOAD_LIVE_EDITING_FILTER;

    constructor(readonly filterId: string) {
    }
}

export class LoadLiveEditingFilterFailureAction implements Action {
    public readonly type = LOAD_LIVE_EDITING_FILTER_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class SetLiveEditingFilterAction implements Action {
    public readonly type = SET_LIVE_EDITING_FILTER;

    constructor(readonly filter: FilterConfiguration) {
    }
}

export class SetLiveEditingFilterFailureAction implements Action {
    public readonly type = SET_LIVE_EDITING_FILTER_FAILURE;

    constructor(readonly cause: any) {
    }
}