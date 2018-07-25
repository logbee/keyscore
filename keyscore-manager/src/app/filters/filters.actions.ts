import {Action} from "@ngrx/store";
import {FilterConfiguration} from "../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../models/filter-model/FilterInstanceState";
import {Dataset} from "../models/filter-model/dataset/Dataset";

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
export const DRAIN_FILTER_FAILURE = "[Filter] DrainFilterActionFailure";
export const DRAIN_FILTER_SUCCESS = "[Filter] DrainFilterActionSuccess";
export const INSERT_DATASETS = "[Filter] InsertDatasetsAction";
export const INSERT_DATASETS_FAILURE = "[Filter] InsertDatasetsFailure";
export const INSERT_DATASETS_SUCCESS = "[Filter] InsertDatasetsSuccess";
export const EXTRACT_DATASETS = "[Filter] ExtractDatasetsAction";
export const EXTRACT_DATASETS_FAILURE = "[Filter] ExtractDatasetsFailure";
export const EXTRACT_DATASETS_INITIAL_SUCCESS = "[Filter] ExtractDatasetsInitialSuccess";
export const EXTRACT_DATASETS_RESULT_SUCCESS = "[Filter] ExtractDatasetsResultSuccess";
export const INITIALIZE_LIVE_EDITING_DATA = "[Filter] InitializeLiveEditingDataAction";
export const LOCK_CURRENT_EXAMPLE_DATASET = "[Filter] LockCurrentExampleDatasetAction";
export const RECONFIGURE_FILTER_ACTION = "[Filter] ReconfigureFilterAction";
export const RECONFIGURE_FILTER_SUCCESS = "[Filter] ReconfigureFilterSuccess";
export const RECONFIGURE_FILTER_FAILURE = "[Filter] ReconfigureFilterFailure";
export const UPDATE_FILTER_CONFIGURATION = "[Filter] UpdateFilterConfiguration";
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
    | ExtractDatasetsInitialSuccess
    | ExtractDatasetsResultSuccess
    | InitializeLiveEditingDataAction
    | LockCurrentExampleDatasetAction
    | ReconfigureFilterAction
    | ReconfigureFilterSuccess
    | ReconfigureFilterFailure
    | UpdateFilterConfiguration;

export class LoadLiveEditingFilterAction implements Action {
    public readonly type = LOAD_LIVE_EDITING_FILTER;

    constructor(readonly filterId: string, readonly amount: number) {
    }
}

export class LoadLiveEditingFilterSuccess implements Action {
    public readonly type = LOAD_LIVE_EDITING_FILTER_SUCCESS;

    constructor(readonly filter: FilterConfiguration, readonly filterId: string) {
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

    constructor(readonly filterId: string, readonly pause: boolean) {
    }
}

export class PauseFilterFailure implements Action {
    public readonly type = PAUSE_FILTER_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class PauseFilterSuccess implements Action {
    public readonly type = PAUSE_FILTER_SUCCESS;

    constructor(readonly state: FilterInstanceState) {
    }
}

export class DrainFilterAction implements Action {
    public readonly type = DRAIN_FILTER;

    constructor(readonly filterId: string, readonly drain: boolean) {
    }
}

export class DrainFilterSuccess implements Action {
    public readonly type = DRAIN_FILTER_SUCCESS;

    constructor(readonly state: FilterInstanceState) {
    }
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

    constructor(readonly state: FilterInstanceState) {
    }
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

export class ExtractDatasetsInitialSuccess implements Action {
    public readonly type = EXTRACT_DATASETS_INITIAL_SUCCESS;

    constructor(readonly datasets: Dataset[]) {
    }
}

export class ExtractDatasetsResultSuccess implements Action {
    public readonly type = EXTRACT_DATASETS_RESULT_SUCCESS;

    constructor(readonly datasets: Dataset[]) {
    }
}


export class ExtractDatasetsFailure implements Action {
    public readonly type = EXTRACT_DATASETS_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class InitializeLiveEditingDataAction implements Action {
    public readonly type = INITIALIZE_LIVE_EDITING_DATA;

    constructor(readonly filterId: string) {

    }
}

export class LockCurrentExampleDatasetAction implements Action {
    public readonly type = LOCK_CURRENT_EXAMPLE_DATASET;

    constructor(readonly dataset: Dataset) {
    }
}

export class ReconfigureFilterAction implements Action {
    public readonly type = RECONFIGURE_FILTER_ACTION;

    constructor(readonly filterId: string, readonly configuration: FilterConfiguration) {
    }
}

export class ReconfigureFilterSuccess implements Action {
    public readonly type = RECONFIGURE_FILTER_SUCCESS;

    constructor(readonly state: FilterInstanceState) {
    }
}

export class ReconfigureFilterFailure implements Action {
    public readonly type = RECONFIGURE_FILTER_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class UpdateFilterConfiguration implements Action {
    public readonly type = UPDATE_FILTER_CONFIGURATION;

    constructor(readonly filter: FilterConfiguration, readonly values: any) {
    }
}
