import {createFeatureSelector, createSelector} from "@ngrx/store";


export class StreamsState {
    streamList: Array<StreamModel>;
    editingStream: StreamModel;
    loading: boolean;
    filterDescriptors: FilterDescriptor[];
    filterCategories: string[];
}

export interface StreamModel {
    id: string;
    name: string;
    description: string;
    filters: Array<FilterModel>;
}

export interface FilterModel {
    id: string;
    name: string;
    displayName: string;
    description: string;
    parameters: ParameterDescriptor[];
    isEdited:boolean;
}


export interface FilterDescriptor {
    name: string;
    displayName: string;
    description: string;
    category: string;
    parameters: ParameterDescriptor[];
}

export interface ParameterDescriptor {
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
}

export interface TextParameterDescriptor extends ParameterDescriptor {
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
    validator:string;
}

export interface ListParameterDescriptor extends ParameterDescriptor{
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
    element: ParameterDescriptor;
    min:Number;
    max:Number;
}

export interface MapParameterDescriptor extends ParameterDescriptor{
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
    key: ParameterDescriptor;
    value: ParameterDescriptor;
    min:Number;
    max:Number;
}


export const getStreamsState = createFeatureSelector<StreamsState>('streams');

export const getStreamList = createSelector(getStreamsState, (state: StreamsState) => state.streamList);

export const getEditingStream = createSelector(getStreamsState, (state: StreamsState) => state.editingStream);

export const isLoading = createSelector(getStreamsState, (state: StreamsState) => state.loading);

export const getFilterDescriptors = createSelector(getStreamsState, (state: StreamsState) => state.filterDescriptors);

export const getFilterCategories = createSelector(getStreamsState, (state: StreamsState) => state.filterCategories);

export const getEditedFilterParameters = createSelector(getStreamsState,(state:StreamsState) => state.editingStream.filters.find(f => f.isEdited).parameters);


