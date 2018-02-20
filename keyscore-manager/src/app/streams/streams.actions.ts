import {Action} from "@ngrx/store";
import {FilterDescriptor, StreamModel} from "./streams.model";

export const CREATE_STREAM = '[Stream] CreateStream';
export const EDIT_STREAM = '[Stream] EditStream';
export const RESET_STREAM = '[Stream] ResetStream';
export const UPDATE_STREAM = '[Stream] UpdateStream';
export const DELETE_STREAM = '[Stream] DeleteStream';
export const ADD_FILTER = '[Stream] AddFilter';
export const MOVE_FILTER = '[Stream] MoveFilter';
export const LOAD_FILTER_DESCRIPTORS_SUCCESS = '[Stream] LoadFilterDescriptorsSuccess';
export const LOAD_FILTER_DESCRIPTORS_FAILURE ='[Stream] LoadFilterDescriptorsFailed';
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
    | LoadFilterDescriptorsSuccessAction
    | LoadFilterDescriptorsFailureAction
    | LoadFilterDescriptorsAction

export class CreateStreamAction implements Action {
    readonly type = '[Stream] CreateStream';

    constructor(readonly id: string, readonly name: string, readonly description: string) {

    }
}

export class EditStreamAction implements Action {
    readonly type = '[Stream] EditStream';

    constructor(readonly id: string) {

    }
}

export class ResetStreamAction implements Action {
    readonly type = '[Stream] ResetStream';

    constructor(readonly id: string) {

    }
}

export class UpdateStreamAction implements Action {
    readonly type = '[Stream] UpdateStream';

    constructor(readonly stream: StreamModel) {

    }
}

export class UpdateStreamSuccessAction implements Action {
    readonly type = '[Stream] UpdateSuccessStream';

    constructor(readonly stream: StreamModel) {

    }
}

export class UpdateStreamFailureAction implements Action {
    readonly type = '[Stream] UpdateFailureStream';

    constructor(readonly stream: StreamModel) {

    }
}

export class DeleteStreamAction implements Action {
    readonly type = '[Stream] DeleteStream';

    constructor(readonly id: string) {

    }
}

export class AddFilterAction implements Action {
    readonly type = '[Stream] AddFilter';

    constructor(readonly filter:FilterDescriptor){

    }
}

export class MoveFilterAction implements Action{
    readonly type='[Stream] MoveFilter';

    constructor(readonly filterId:string, readonly position:number){

    }
}

export class LoadFilterDescriptorsSuccessAction implements Action {
    readonly type = '[Stream] LoadFilterDescriptorsSuccess';

    constructor(readonly descriptors: FilterDescriptor[]) {
    }
}

export class LoadFilterDescriptorsFailureAction implements Action {
    readonly type = '[Stream] LoadFilterDescriptorsFailed';

    constructor(readonly cause: any) {
    }
}

export class LoadFilterDescriptorsAction implements Action{
    readonly type = '[Stream] LoadFilterDescriptors'

}
