import {createFeatureSelector, createSelector} from "@ngrx/store";

//-----------------------Pipelines---------------------------

export class PipelinesModuleState {
    pipelines: PipelinesState;
    filter: FilterState;
}

export class PipelinesState {
    pipelineList: Array<PipelineInstance>;
    editingPipeline: InternalPipelineConfiguration;
    editingFilter: FilterConfiguration;
    loading: boolean;
    filterDescriptors: FilterDescriptor[];
    filterCategories: string[];
    editingPipelineIsLocked: boolean;
}

export interface FilterState {
    filterId: string
}

export interface PipelineInstance{
    id:string;
    name: string;
    description:string;
    configurationId:string;
    health:Health;
}

export enum Health{
    Green,
    Yellow,
    Red
}

/*export interface PipelineModel {
    id: string;
    name: string;
    description: string;
    filters: Array<FilterModel>;
    domRepresentation?: any;
}*/

export interface InternalPipelineConfiguration{
    id:string;
    name:string;
    description:string;
    filters:FilterConfiguration[];
}

export interface PipelineConfiguration {
    id: string;
    name: string;
    description: string;
    source: FilterConfiguration;
    filter: FilterConfiguration[];
    sink: FilterConfiguration;
}

//-----------------------Filter---------------------------
export interface FilterConfiguration {
    id: string;
    descriptor: FilterDescriptor
    parameters: Parameter[];
}

/*export interface FilterModel {
    id: string;
    name: string;
    displayName: string;
    description: string;
    parameters: ParameterDescriptor[];
}*/


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


//------------------Parameter Descriptors------------------

export interface ParameterDescriptor {
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
    value?: any;
}

//------------------Parameter for Configuration------------------

export interface Parameter {
    name: string;
    value: any;
    parameterType: string;
}

export const getPipelinesModuleState = createFeatureSelector<PipelinesModuleState>('pipelines');

export const getPipelinesState = createSelector(getPipelinesModuleState, (state: PipelinesModuleState) => state.pipelines);

export const getFilterState = createSelector(getPipelinesModuleState, (state: PipelinesModuleState) => state.filter);

export const getFilterId = createSelector(getFilterState, (state: FilterState) => state.filterId);

export const getPipelineList = createSelector(getPipelinesState, (state: PipelinesState) => state.pipelineList);

export const getEditingPipeline = createSelector(getPipelinesState, (state: PipelinesState) => state.editingPipeline);

export const getEditingPipelineIsLocked = createSelector(getPipelinesState, (state: PipelinesState) => state.editingPipelineIsLocked);

export const isLoading = createSelector(getPipelinesState, (state: PipelinesState) => state.loading);

export const getFilterDescriptors = createSelector(getPipelinesState, (state: PipelinesState) => state.filterDescriptors);

export const getFilterCategories = createSelector(getPipelinesState, (state: PipelinesState) => state.filterCategories);

export const getEditingFilterParameters = createSelector(getPipelinesState, (state: PipelinesState) => state.editingFilter.parameters);

export const getEditingFilter = createSelector(getPipelinesState, (state: PipelinesState) => state.editingFilter);

export const getFilterById = (id) => createSelector(getPipelinesState, (state: PipelinesState) => state.editingPipeline.filters.find((filter: FilterConfiguration) => filter.id === id));
