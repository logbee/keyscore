import {Action} from "@ngrx/store";
import {FilterDescriptor, FilterModel} from "../streams.model";
import {Observable} from "rxjs/index";
import {a} from "@angular/core/src/render3";

export const LOAD_FILTER_DESCRIPTOR_SUCCESS = '[Filter] LoadFilterDescriptorSuccess';
export const LOAD_FILTER_DESCRIPTOR = '[Filter] LoadFilterDescriptor';
export const LOAD_FILTER_DESCRIPTOR_FAILURE = '[Filter] LoadFilterDescriptorFailure';
export const LOAD_FILTER_MODEL_FROM_STREAM = '[Filter] LoadFilterModelFromStream';

export type FiltersActions =
|LoadFilterDescriptorAction
|LoadFilterDescriptorSuccessAction
|LoadFilterDescriptorFailureAction
|LoadFilterModelFromStreamAction

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

export class LoadFilterModelFromStreamAction  implements Action {
    readonly type = LOAD_FILTER_MODEL_FROM_STREAM;

    constructor(readonly filterModel: FilterModel) {

    }
}