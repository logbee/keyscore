import {Action} from "@ngrx/store";
import {FilterDescriptor, FilterModel} from "../streams.model";

export const LOAD_FILTER_DESCRIPTOR_SUCCESS = '[Filter] LoadFilterDescriptorSuccess';
export const LOAD_FILTER_DESCRIPTOR = '[Filter] LoadFilterDescriptor';
export const LOAD_FILTER_DESCRIPTOR_FAILURE = '[Filter] LoadFilterDescriptorFailure';
export const CONFIGURE_FILTER = '[Filter] ConfigureFilter';

export type FiltersActions =
|LoadFilterDescriptorAction
|LoadFilterDescriptorSuccessAction
|LoadFilterDescriptorFailureAction
|ConfigureFilterAction

export class LoadFilterDescriptorAction implements Action {
    readonly type = LOAD_FILTER_DESCRIPTOR;

    constructor(readonly  filterName: string) {

    }
}

export class LoadFilterDescriptorFailureAction implements Action {
    readonly type = LOAD_FILTER_DESCRIPTOR_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class LoadFilterDescriptorSuccessAction implements Action {
    readonly type = LOAD_FILTER_DESCRIPTOR_SUCCESS;

    constructor(readonly  filterDescriptor: FilterDescriptor) {

    }
}


export class ConfigureFilterAction implements Action {
    readonly  type = CONFIGURE_FILTER;

    constructor(readonly id: string) {

    }

}