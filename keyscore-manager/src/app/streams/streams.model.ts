import {createFeatureSelector, createSelector} from "@ngrx/store";


export class StreamsState {
    streamList: Array<StreamModel>;
    editingStream: StreamModel;
    loading: boolean;
    filterDescriptors: FilterDescriptor[];
    filterCategories: string[];
}

export interface StreamModel {
    id: string,
    name: string,
    description: string,
    filters: Array<FilterModel>
}

export interface FilterModel {
    id: string,
    name: string,
    description: string
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

export interface BooleanParameterDescriptor extends ParameterDescriptor {

}

export interface TextParameterDescriptor extends ParameterDescriptor {

}


export const getStreamsState = createFeatureSelector<StreamsState>('streams');

export const getStreamList = createSelector(getStreamsState, (state: StreamsState) => state.streamList);

export const getEditingStream = createSelector(getStreamsState, (state: StreamsState) => state.editingStream);

export const isLoading = createSelector(getStreamsState, (state: StreamsState) => state.loading);

export const getFilterDescriptors = createSelector(getStreamsState, (state: StreamsState) => state.filterDescriptors);

export const getFilterCategories = createSelector(getStreamsState, (state: StreamsState) => state.filterCategories);


