import {createFeatureSelector, createSelector} from "@ngrx/store";


export class StreamsState {
    streamList: Array<StreamModel>;
    editingStream: StreamModel;
    editingFilter:FilterModel;
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
    category: string;
    parameters: ParameterDescriptor[];
}

//------------------Parameter Descriptors------------------

export interface ParameterDescriptor {
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
    value?:any;
}

export interface ListParameterDescriptor extends ParameterDescriptor{
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
    element: ParameterDescriptor;
    min:Number;
    max:Number;
    value?:string[];
}

export interface MapParameterDescriptor extends ParameterDescriptor{
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
    mapKey: ParameterDescriptor;
    mapValue: ParameterDescriptor;
    min:Number;
    max:Number;
    value?:any;
}

//------------------Parameter------------------

export interface Parameter{
    name:string;
    displayName:string;
    mandatory:boolean;
    kind:string;
    value?:any;
}

export interface TextParameter extends Parameter{
    validator:string;
    value?:string;
}

export interface IntParameter extends Parameter{
    value?:number;
}

export interface BooleanParameter extends Parameter{
    value?:boolean;
}


export interface ListParameter extends Parameter{
    min:number;
    max:number;

}

export interface TextListParameter extends ListParameter {
    validator:string;
    value?:string[];
}
export interface MapParameter extends Parameter{
    min:number;
    max:number;
}

export interface TextMapParameter extends MapParameter{
    keyValidator:string;
    valueValidator:string;
    value?:Map<string,string>;
}


export const getStreamsState = createFeatureSelector<StreamsState>('streams');

export const getStreamList = createSelector(getStreamsState, (state: StreamsState) => state.streamList);

export const getEditingStream = createSelector(getStreamsState, (state: StreamsState) => state.editingStream);

export const getEditingStreamIsLocked = createSelector(getStreamsState,(state:StreamsState) => state.editingStreamIsLocked);

export const isLoading = createSelector(getStreamsState, (state: StreamsState) => state.loading);

export const getFilterDescriptors = createSelector(getStreamsState, (state: StreamsState) => state.filterDescriptors);

export const getFilterCategories = createSelector(getStreamsState, (state: StreamsState) => state.filterCategories);

export const getEditingFilterParameters = createSelector(getStreamsState,(state:StreamsState) => state.editingFilter.parameters);

export const getEditingFilter = createSelector(getStreamsState,(state:StreamsState) => state.editingFilter);


