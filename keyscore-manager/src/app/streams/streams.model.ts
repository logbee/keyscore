import {createFeatureSelector, createSelector} from "@ngrx/store";

//-----------------------Streams---------------------------

export class StreamsModuleState {
    streams: StreamsState;
    filter: FilterState;
}

export class StreamsState {
    streamList: Array<StreamModel>;
    editingStream: StreamModel;
    editingFilter: FilterModel;
    loading: boolean;
    filterDescriptors: FilterDescriptor[];
    filterCategories: string[];
    editingStreamIsLocked: boolean;
}

export interface FilterState {
    currentFilter: FilterDescriptor
}

export interface StreamModel {
    id: string;
    name: string;
    description: string;
    filters: Array<FilterModel>;
}

export interface StreamConfiguration {
    id: string;
    name: string;
    description: string;
    source: FilterConfiguration;
    sink: FilterConfiguration;
    filter: FilterConfiguration[];
}
//-----------------------Filter---------------------------
export interface FilterConfiguration {
    id: string;
    kind: string;
    parameters: Parameter[];
}

export interface FilterModel {
    id: string;
    name: string;
    displayName: string;
    description: string;
    parameters: ParameterDescriptor[];
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
export const getFilterState = createFeatureSelector<FilterState>('filters');

export const getCurrentFilter = createSelector(getFilterState, (state: FilterState) => state.currentFilter);



export const getStreamsState = createFeatureSelector<StreamsModuleState>('streams');

export const getStreamList = createSelector(getStreamsState, (state: StreamsModuleState) => state.streams.streamList);

export const getEditingStream = createSelector(getStreamsState, (state: StreamsModuleState) => state.streams.editingStream);

export const getEditingStreamIsLocked = createSelector(getStreamsState, (state: StreamsModuleState) => state.streams.editingStreamIsLocked);

export const isLoading = createSelector(getStreamsState, (state: StreamsModuleState) => state.streams.loading);

export const getFilterDescriptors = createSelector(getStreamsState, (state: StreamsModuleState) => state.streams.filterDescriptors);

export const getFilterCategories = createSelector(getStreamsState, (state: StreamsModuleState) => state.streams.filterCategories);

export const getEditingFilterParameters = createSelector(getStreamsState, (state: StreamsModuleState) => state.streams.editingFilter.parameters);

export const getEditingFilter = createSelector(getStreamsState, (state: StreamsModuleState) => state.streams.editingFilter);


