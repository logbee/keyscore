import {Action} from "@ngrx/store";
import {FilterDescriptor} from "../streams/streams.model";

export const SET_CURRENT_FILTER = '[Filter] SetCurrentFilter';
export const GET_CURRENT_DESCRIPTOR = '[Filter] GetCurrentDescriptor';
export const GET_CURRENT_DESCRIPTOR_FAILURE = '[Filter] GetCurrentDescriptorFailure';

export type FiltersActions =
|GetCurrentDescriptorAction
|SetCurrentFilterAction
|GetCurrentDescriptorFailureAction

export class GetCurrentDescriptorAction implements Action {
    readonly type = GET_CURRENT_DESCRIPTOR;

    constructor(readonly  filterName: string) {

    }
}

export class GetCurrentDescriptorFailureAction implements Action {
    readonly type = GET_CURRENT_DESCRIPTOR_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class SetCurrentFilterAction implements Action {
    readonly type = SET_CURRENT_FILTER;

    constructor(readonly  filterDescriptor: FilterDescriptor) {

    }
}