import {Action} from "@ngrx/store";
import {FilterDescriptor, FilterModel, StreamModel} from "./streams.model";

export const CREATE_STREAM = '[Stream] CreateStream';
export const EDIT_STREAM = '[Stream] EditStream';
export const LOCK_EDITING_STREAM = '[Stream] LockEditingStream';
export const RESET_STREAM = '[Stream] ResetStream';
export const UPDATE_STREAM = '[Stream] UpdateStream';
export const UPDATE_STREAM_SUCCESS = '[Stream] UpdateStreamSuccess';
export const UPDATE_STREAM_FAILURE = '[Stream] UpdateStreamFailure';
export const DELETE_STREAM = '[Stream] DeleteStream';
export const ADD_FILTER = '[Stream] AddFilter';
export const MOVE_FILTER = '[Stream] MoveFilter';
export const EDIT_FILTER = '[Stream] EditFilter';
export const REMOVE_FILTER = '[Stream] RemoveFilter';
export const UPDATE_FILTER = '[Stream] UpdateFilter';
export const LOAD_FILTER_DESCRIPTORS_SUCCESS = '[Stream] LoadFilterDescriptorsSuccess';
export const LOAD_FILTER_DESCRIPTORS_FAILURE = '[Stream] LoadFilterDescriptorsFailed';
export const LOAD_FILTER_DESCRIPTORS = '[Stream] LoadFilterDescriptors';


export type StreamActions =
    | CreateStreamAction
    | EditStreamAction
    | ResetStreamAction
    | UpdateStreamAction
    | UpdateStreamSuccessAction
    | UpdateStreamFailureAction
    | DeleteStreamAction
    | AddFilterAction
    | MoveFilterAction
    | UpdateFilterAction
    | LoadFilterDescriptorsSuccessAction
    | LoadFilterDescriptorsFailureAction
    | LoadFilterDescriptorsAction
    | EditFilterAction
    | RemoveFilterAction
    | LockEditingStreamAction

export class CreateStreamAction implements Action {
    readonly type = CREATE_STREAM;

    constructor(readonly id: string, readonly name: string, readonly description: string) {

    }
}

export class EditStreamAction implements Action {
    readonly type = EDIT_STREAM;

    constructor(readonly id: string) {

    }
}

export class LockEditingStreamAction implements Action {
    readonly type = LOCK_EDITING_STREAM;

    constructor(readonly isLocked: boolean) {
    }
}

export class ResetStreamAction implements Action {
    readonly type = RESET_STREAM;

    constructor(readonly id: string) {

    }
}

export class UpdateStreamAction implements Action {
    readonly type = UPDATE_STREAM;

    constructor(readonly stream: StreamModel) {

    }
}

export class UpdateStreamSuccessAction implements Action {
    readonly type = UPDATE_STREAM_SUCCESS;

    constructor(readonly stream: StreamModel) {

    }
}

export class UpdateStreamFailureAction implements Action {
    readonly type = UPDATE_STREAM_FAILURE;

    constructor(readonly stream: StreamModel) {

    }
}

export class DeleteStreamAction implements Action {
    readonly type = DELETE_STREAM;

    constructor(readonly id: string) {

    }
}

export class AddFilterAction implements Action {
    readonly type = ADD_FILTER;

    constructor(readonly filter: FilterDescriptor) {

    }
}

export class MoveFilterAction implements Action {
    readonly type = MOVE_FILTER;

    constructor(readonly filterId: string, readonly position: number) {

    }
}

export class EditFilterAction implements Action {
    readonly type = EDIT_FILTER;

    constructor(readonly filterId: string) {

    }
}

export class UpdateFilterAction implements Action {
    readonly type = UPDATE_FILTER;

    constructor(readonly filter: FilterModel, readonly values: any) {
    }
}

export class RemoveFilterAction implements Action {
    readonly type = REMOVE_FILTER;

    constructor(readonly filterId: string) {

    }
}


export class LoadFilterDescriptorsAction implements Action {
    readonly type = LOAD_FILTER_DESCRIPTORS

}

export class LoadFilterDescriptorsSuccessAction implements Action {
    readonly type = LOAD_FILTER_DESCRIPTORS_SUCCESS;

    constructor(readonly descriptors: FilterDescriptor[]) {
    }
}

export class LoadFilterDescriptorsFailureAction implements Action {
    readonly type = LOAD_FILTER_DESCRIPTORS_FAILURE;

    constructor(readonly cause: any) {
    }
}
