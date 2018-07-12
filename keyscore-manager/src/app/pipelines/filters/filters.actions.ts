import {Action} from "@ngrx/store";
import {FilterConfiguration, FilterInstanceState} from "../pipelines.model";
import {s} from "@angular/core/src/render3";

export const LOAD_LIVE_EDITING_FILTER = "[Filter] LoadLiveEditingFilter";
export const LOAD_LIVE_EDITING_FILTER_FAILURE = "[Filter] LoadLiveEditingFilterFailure";
export const LOAD_LIVE_EDITING_FILTER_SUCCESS = "[Filter] LoadLiveEditingFilterSuccess";
export const LOAD_FILTERSTATE = "[Filter] LoadFilterState";
export const LOAD_FILTERSTATE_FAILURE = "[Filter] LoadFilterStateFailure";
export const LOAD_FILTERSTATE_SUCCESS = "[Filter] LoadFilterStateSuccess";
export const PAUSE_FILTER = "[Filter] PauseFilterAction";
export const PAUSE_FILTER_SUCCESS = "[Filter] PauseFilterSuccess";
export const PAUSE_FILTER_FAILURE = "[Filter] PauseFilterFailure";
export const DRAIN_FILTER = "[Filter] DrainFilterAction";
export const DRAIN_FILTER_FAILURE = "[Filter] DrainFilterAction";
export const DRAIN_FILTER_SUCCESS = "[Filter] DrainFilterAction";
export const INSERT_DATASETS = "[Filter] InsertDatasetsAction";
export const INSERT_DATASETS_FAILURE = "[Filter] InsertDatasetsFailure";
export const INSERT_DATASETS_SUCCESS = "[Filter] InsertDatasetsSuccess";
export const EXTRACT_DATASETS = "[Filter] ExtractDatasetsAction";
export const EXTRACT_DATASETS_FAILURE = "[Filter] ExtractDatasetsFailure";
export const EXTRACT_DATASETS_SUCCESS = "[Filter] ExtractDatasetsSuccess";

export type FiltersActions =

    | LoadLiveEditingFilterAction
    | LoadLiveEditingFilterFailure
    | LoadLiveEditingFilterSuccess
    | LoadFilterStateAction
    | LoadFilterStateFailure
    | LoadFilterStateSuccess
    | PauseFilterAction
    | PauseFilterSuccess
    | PauseFilterFailure
    | DrainFilterAction
    | DrainFilterSuccess
    | DrainFilterFailure
    | InsertDatasetsAction
    | InsertDatasetsFailure
    | InsertDatasetsSuccess
    | ExtractDatasetsAction
    | ExtractDatasetsFailure
    | ExtractDatasetsSuccess;

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

export class LoadFilterStateAction implements Action {
    public readonly type = LOAD_FILTERSTATE;

    constructor(readonly filterId: string) {
    }
}

export class LoadFilterStateFailure implements Action {
    public readonly type = LOAD_FILTERSTATE_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class LoadFilterStateSuccess implements Action {
    public readonly type = LOAD_FILTERSTATE_SUCCESS;

    constructor(readonly state: FilterInstanceState) {
    }
}

export class PauseFilterAction implements Action {
    public readonly type = PAUSE_FILTER;

    constructor(readonly filterId: string) {
    }
}

export class PauseFilterFailure implements Action {
    public readonly type = PAUSE_FILTER_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class PauseFilterSuccess implements Action {
    public readonly type = PAUSE_FILTER_SUCCESS;
}

export class DrainFilterAction implements Action {
    public readonly type = DRAIN_FILTER;

    constructor(readonly filterId: string) {
    }
}

export class DrainFilterSuccess implements Action {
    public readonly type = DRAIN_FILTER_SUCCESS;
}

export class DrainFilterFailure implements Action {
    public readonly type = DRAIN_FILTER_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class InsertDatasetsAction implements Action {
    public readonly type = INSERT_DATASETS;

    constructor(readonly filterId: string, readonly datasets: string[]) {
    }
}

export class InsertDatasetsSuccess implements Action {
    public readonly type = INSERT_DATASETS_SUCCESS;
}

export class InsertDatasetsFailure implements Action {
    public readonly type = INSERT_DATASETS_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class ExtractDatasetsAction implements Action {
    public readonly type = EXTRACT_DATASETS;

    constructor(readonly filterId: string, readonly amount: number) {
    }
}

export class ExtractDatasetsSuccess implements Action {
    public readonly type = EXTRACT_DATASETS_SUCCESS;

    constructor(readonly datasets: string[]) {
    }
}

export class ExtractDatasetsFailure implements Action {
    public readonly type = EXTRACT_DATASETS_FAILURE;
    constructor(readonly cause: any) {
    }
}
