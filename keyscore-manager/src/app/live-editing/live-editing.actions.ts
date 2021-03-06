import {Action, Store} from "@ngrx/store";
import {Configuration} from "../models/common/Configuration";
import {ResourceInstanceState} from "../models/filter-model/ResourceInstanceState";
import {Dataset} from "../models/dataset/Dataset";
import {Blueprint} from "../models/blueprints/Blueprint";
import {Descriptor} from "../models/descriptors/Descriptor";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {DatasetTableModel} from "../models/dataset/DatasetTableModel";

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
export const EXTRACT_OUTPUT_DATASETS_SUCESS = "[Filter] ExtractOutputDatasetsSucces";
export const EXTRACT_INPUT_DATASETS_SUCESS = "[Filter] ExtractInputDatasetsSucces";
export const EXTRACT_DATASETS_RESULT_SUCCESS = "[Filter] ExtractDatasetsResultSuccess";
export const INITIALIZE_LIVE_EDITING_DATA = "[Filter] InitializeLiveEditingDataAction";
export const LOCK_CURRENT_EXAMPLE_DATASET = "[Filter] LockCurrentExampleDatasetAction";
export const RECONFIGURE_FILTER_ACTION = "[Filter] ReconfigureFilterAction";
export const RECONFIGURE_FILTER_SUCCESS = "[Filter] ReconfigureFilterSuccess";
export const RECONFIGURE_FILTER_FAILURE = "[Filter] ReconfigureFilterFailure";
export const UPDATE_FILTER_CONFIGURATION = "[Filter] UpdateFilterConfiguration";
export const RESTORE_FILTER_CONFIGURATION = "[Filter] RestoreFilterConfiguration";
export const UPDATE_DATASET_COUNTER = "[Filter] UpdateDatasetCounter";
export const LOAD_FILTER_BLUEPRINT_SUCCESS = "[Filter] LoadFilterBlueprintSuccess";
export const LOAD_FILTER_BLUEPRINT_FAILURE = "[Filter] LoadFilterBlueprintFailure";
export const LOAD_FILTER_CONFIGURATION = "[Filter] LoadFilterConfiguration";
export const LOAD_FILTER_CONFIGURATION_SUCCESS = "[Filter] LoadFilterConfigurationSuccess";
export const LOAD_FILTER_CONFIGURATION_FAILURE = "[Filter] LoadFilterConfigurationFailure";
export const LOAD_DESCRIPTOR_FOR_BLUEPRINT = "[Filter] LoadDescriptorForBlueprint";
export const LOAD_DESCRIPTOR_FOR_BLUEPRINT_SUCCESS = "[Filter] LoadDescriptorForBlueprintSuccess";
export const RESOLVED_DESCRIPTOR_FOR_BLUEPRINT = "[Filter] ResolvedDescriptorForBlueprint";
export const SAVE_UPDATED_CONFIGURATION = "[Filter] SaveUpdatedConfiguration";
export const RESET_ACTION = "[Filter] ResetAction";
export const LOAD_ALL_PIPELINES_FOR_REDIRECT = "[Filter] LoadAllPipelinesForRedirect";
export const NAVIAGATE_TO_PIPELY_FAILURE = "[Filter] NaviagatetoPipelyFailure";
export const INITIAL_EXTRACT_SUCCESS = "[Filter] InitialExtractSuccess";
export const UPDATE_CONFIGURATION_IN_BACKEND = "[Filter] UpdateConfigurationInBackend";
export const OVERWRITE_SUCCESS = "[Filter] OverwriteSuccess";
export const START_CONFIGURATION_POLLING = "[Filter] StartConfigurationPolling";
export const STORE_CURRENT_DATASET = "[Filter] StoreCurrentDataset";
export const STORE_CURRENT_RECORD_INDEX = "[Filter] StoreCurrentRecordIndex";

export type LiveEditingActions =

    | LoadFilterConfigurationAction
    | LoadFilterConfigurationFailure
    | LoadFilterConfigurationSuccess
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
    | ExtractDatasetsResultSuccess
    | InitializeLiveEditingDataAction
    | LockCurrentExampleDatasetAction
    | ReconfigureFilterAction
    | ReconfigureFilterSuccess
    | ReconfigureFilterFailure
    | UpdateFilterConfiguration
    | RestoreFilterConfiguration
    | UpdateDatasetCounter
    | LoadFilterBlueprintSuccess
    | LoadDescriptorForBlueprint
    | LoadDescriptorForBlueprintSuccess
    | ResolvedDescriptorForBlueprintSuccess
    | SaveUpdatedConfiguration
    | ResetAction
    | LoadAllPipelinesForRedirect
    | NaviagatetoPipelyFailure
    | InitialExtractSuccess
    | UpdateConfigurationInBackend
    | OverwriteSuccess
    | StoreCurrentDataset
    | StoreCurrentRecordIndex;


export class StoreCurrentRecordIndex implements Action {
    public readonly type = STORE_CURRENT_RECORD_INDEX;

    constructor(readonly index: number) {

    }

}
export class StoreCurrentDataset implements Action {
    public readonly type = STORE_CURRENT_DATASET;

    constructor(readonly dataset: DatasetTableModel) {

    }
}

export class OverwriteSuccess implements Action{
    public readonly type = OVERWRITE_SUCCESS;
}

export class UpdateConfigurationInBackend implements Action{
    public readonly type     = UPDATE_CONFIGURATION_IN_BACKEND;

    constructor(readonly configuration: Configuration) {

    }
}
export class LoadAllPipelinesForRedirect implements Action{
    public readonly type = LOAD_ALL_PIPELINES_FOR_REDIRECT;
}

export class NaviagatetoPipelyFailure implements Action{
    public readonly type = NAVIAGATE_TO_PIPELY_FAILURE;

    constructor(readonly cause: any) {

    }
}

export class LoadFilterConfigurationAction implements Action {
    public readonly type = LOAD_FILTER_CONFIGURATION;

    constructor(readonly filterId: string) {
    }
}

export class LoadFilterConfigurationSuccess implements Action {
    public readonly type = LOAD_FILTER_CONFIGURATION_SUCCESS;

    constructor(readonly configuration: Configuration, readonly filterId: string) {
    }
}

export class LoadFilterConfigurationFailure implements Action {
    public readonly type = LOAD_FILTER_CONFIGURATION_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class LoadFilterStateAction implements Action {
    public readonly type = LOAD_FILTERSTATE;

    constructor(readonly filterId: string, readonly amount:number) {
    }
}

export class LoadFilterStateFailure implements Action {
    public readonly type = LOAD_FILTERSTATE_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class LoadFilterStateSuccess implements Action {
    public readonly type = LOAD_FILTERSTATE_SUCCESS;

    constructor(readonly state: ResourceInstanceState) {
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

    constructor(readonly state: ResourceInstanceState) {
    }
}

export class DrainFilterAction implements Action {
    public readonly type = DRAIN_FILTER;

    constructor(readonly filterId: string, readonly drain: boolean) {
    }
}

export class DrainFilterSuccess implements Action {
    public readonly type = DRAIN_FILTER_SUCCESS;

    constructor(readonly state: ResourceInstanceState) {
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

    constructor(readonly state: ResourceInstanceState) {
    }
}

export class InsertDatasetsFailure implements Action {
    public readonly type = INSERT_DATASETS_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class InitialExtractSuccess implements Action {
    public readonly type = INITIAL_EXTRACT_SUCCESS;

    constructor(readonly input: Dataset[], readonly output: Dataset[]) {

    }
}


export class ExtractDatasetsAction implements Action {
    public readonly type = EXTRACT_DATASETS;

    constructor(readonly filterId: string, readonly amount: number) {
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

    constructor(readonly filterId: string, readonly configuration: Configuration) {
    }
}

export class ReconfigureFilterSuccess implements Action {
    public readonly type = RECONFIGURE_FILTER_SUCCESS;

    constructor(readonly state: ResourceInstanceState) {
    }
}

export class ReconfigureFilterFailure implements Action {
    public readonly type = RECONFIGURE_FILTER_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class UpdateFilterConfiguration implements Action {
    public readonly type = UPDATE_FILTER_CONFIGURATION;

    constructor(readonly configuration: Configuration) {
    }
}

export class RestoreFilterConfiguration implements Action {
    public readonly type = RESTORE_FILTER_CONFIGURATION;

    constructor() {
    }
}

export class UpdateDatasetCounter implements Action {
    public readonly type = UPDATE_DATASET_COUNTER;

    constructor(readonly counter: number) {
    }
}

export class LoadFilterBlueprintSuccess implements Action {
    public readonly type = LOAD_FILTER_BLUEPRINT_SUCCESS;

    constructor(readonly blueprint: Blueprint) {
    }
}

export class LoadFilterBlueprintFailure implements Action {
    public readonly type = LOAD_FILTER_BLUEPRINT_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class LoadDescriptorForBlueprint implements Action {
    public readonly type = LOAD_DESCRIPTOR_FOR_BLUEPRINT;

    constructor(readonly uuid: string) {

    }
}

export class ResolvedDescriptorForBlueprintSuccess implements Action {
    public readonly type = RESOLVED_DESCRIPTOR_FOR_BLUEPRINT;

    constructor(readonly descriptor: ResolvedFilterDescriptor) {

    }
}

export class LoadDescriptorForBlueprintSuccess implements Action {
    public readonly type = LOAD_DESCRIPTOR_FOR_BLUEPRINT_SUCCESS;

    constructor(readonly descriptor: Descriptor) {

    }
}

export class SaveUpdatedConfiguration implements Action {
    public readonly type = SAVE_UPDATED_CONFIGURATION;

    constructor(readonly configuration: Configuration) {
    }
}

export class ResetAction implements Action {
    public readonly type = RESET_ACTION;
}