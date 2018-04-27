import {createFeatureSelector, createSelector} from "@ngrx/store";


export class StreamsState {
    streamList: Array<StreamModel>;
    editingStream: StreamModel;
    editingFilter: FilterModel;
    loading: boolean;
    filterDescriptors: FilterDescriptor[];
    filterCategories: string[];
    editingStreamIsLocked: boolean;
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
    connectionType: string;
}

//------------------Parameter Descriptors------------------

export interface ParameterDescriptor {
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
    value?: any;
}

export interface ListParameterDescriptor extends ParameterDescriptor {
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
    element: ParameterDescriptor;
    min: Number;
    max: Number;
    value?: string[];
}

export interface MapParameterDescriptor extends ParameterDescriptor {
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
    mapKey: ParameterDescriptor;
    mapValue: ParameterDescriptor;
    min: Number;
    max: Number;
    value?: any;
}

//------------------Parameter------------------

export interface Parameter {
    name: string;
    value: any;
    parameterType: string;
}


export const getStreamsState = createFeatureSelector<StreamsState>('streams');

export const getStreamList = createSelector(getStreamsState, (state: StreamsState) => state.streamList);

export const getEditingStream = createSelector(getStreamsState, (state: StreamsState) => state.editingStream);

export const getEditingStreamIsLocked = createSelector(getStreamsState, (state: StreamsState) => state.editingStreamIsLocked);

export const isLoading = createSelector(getStreamsState, (state: StreamsState) => state.loading);

export const getFilterDescriptors = createSelector(getStreamsState, (state: StreamsState) => state.filterDescriptors);

export const getFilterCategories = createSelector(getStreamsState, (state: StreamsState) => state.filterCategories);

export const getEditingFilterParameters = createSelector(getStreamsState, (state: StreamsState) => state.editingFilter.parameters);

export const getEditingFilter = createSelector(getStreamsState, (state: StreamsState) => state.editingFilter);


