import {Action} from "@ngrx/store";
import {FilterDescriptor} from "../streams.model";

export const LOAD_FILTER_DESCRIPTOR_SUCCESS = '[Filter] LoadFilterDescriptorSuccess';
export const LOAD_FILTER_DESCRIPTOR = '[Filter] LoadFilterDescriptor';
export const LOAD_FILTER_DESCRIPTOR_FAILURE = '[Filter] LoadFilterDescriptorFailure';

export type FiltersActions =
|LoadFilterDescriptorAction
|LoadFilterDescriptorSuccessAction
|LoadFilterDescriptorFailureAction

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