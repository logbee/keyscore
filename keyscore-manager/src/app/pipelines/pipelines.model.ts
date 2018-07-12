import {createFeatureSelector, createSelector} from "@ngrx/store";

// -----------------------Pipelines---------------------------

export class PipelinesModuleState {
    public pipelines: PipelinesState;
    public filter: FilterState;
}

export class PipelinesState {
    public pipelineList: PipelineInstance[];
    public editingPipeline: InternalPipelineConfiguration;
    public editingFilter: FilterConfiguration;
    public filterDescriptors: FilterDescriptor[];
    public filterCategories: string[];
    public editingPipelineIsLocked: boolean;
    public pipelineInstancePolling: boolean;
    public wasLastUpdateSuccessful: boolean[];
}

export interface FilterState {
    filter: FilterConfiguration;
}

export interface PipelineInstance {
    id: string;
    name: string;
    description: string;
    configurationId: string;
    health: Health;
}

export enum Health {
    Green = "Green",
    Yellow = "Yellow",
    Red = "Red"
}

export interface InternalPipelineConfiguration {
    id: string;
    name: string;
    description: string;
    filters: FilterConfiguration[];
    isRunning: boolean;
}

export interface PipelineConfiguration {
    id: string;
    name: string;
    description: string;
    source: FilterConfiguration;
    filter: FilterConfiguration[];
    sink: FilterConfiguration;
}

// -----------------------Filter---------------------------
export interface FilterConfiguration {
    id: string;
    descriptor: FilterDescriptor;
    parameters: Parameter[];
}

export interface FilterDescriptor {
    name: string;
    displayName: string;
    description: string;
    previousConnection: FilterConnection;
    nextConnection: FilterConnection;
    parameters: ParameterDescriptor[];
    category: string;
}

export interface FilterConnection {
    isPermitted: boolean;
    connectionType: string[];
}
export interface FilterInstanceState {
    id: string;
    health: Health;
    throughPutTime: number;
    toalThroughPutTime: number;
    status: FilterStatus;
}

export enum FilterStatus {
    Unknown = "Unknown",
    Paused = "Paused",
    Running = "Running",
    Drained = "Drained"
}

// ------------------Parameter Descriptors------------------

export interface ParameterDescriptor {
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
    value?: any;
}

// ------------------Parameter for Configuration------------------

export interface Parameter {
    name: string;
    value: any;
    jsonClass: string;
}

export const getPipelinesModuleState = createFeatureSelector<PipelinesModuleState>("pipelines");

export const getPipelinesState = createSelector(getPipelinesModuleState,
    (state: PipelinesModuleState) => state.pipelines);

export const getFilterState = createSelector(getPipelinesModuleState,
    (state: PipelinesModuleState) => state.filter);

export const getFilterId = createSelector(getFilterState,
    (state: FilterState) => state.filter.id);

export const getLiveEditingFilter = createSelector(getFilterState, (state: FilterState) => state.filter);

export const getPipelineList = createSelector(getPipelinesState,
    (state: PipelinesState) => state.pipelineList);

export const getEditingPipeline = createSelector(getPipelinesState,
    (state: PipelinesState) => state.editingPipeline);

export const getEditingPipelineIsLocked = createSelector(getPipelinesState,
    (state: PipelinesState) => state.editingPipelineIsLocked);

export const getFilterDescriptors = createSelector(getPipelinesState,
    (state: PipelinesState) => state.filterDescriptors);

export const getFilterCategories = createSelector(getPipelinesState,
    (state: PipelinesState) => state.filterCategories);

export const getEditingFilterParameters = createSelector(getPipelinesState,
    (state: PipelinesState) => state.editingFilter.parameters);

export const getEditingFilter = createSelector(getPipelinesState,
    (state: PipelinesState) => state.editingFilter);

export const getPipelinePolling = createSelector(getPipelinesState,
    (state: PipelinesState) => state.pipelineInstancePolling);

export const getLastUpdateSuccess = createSelector(getPipelinesState,
    (state: PipelinesState) => state.wasLastUpdateSuccessful);
