import {Action} from "@ngrx/store";
import {StreamModel} from "./streams.model";

export const CREATE_STREAM = '[Stream] CreateStream';
export const EDIT_STREAM = '[Stream] EditStream';
export const RESET_STREAM = '[Stream] ResetStream';
export const UPDATE_STREAM = '[Stream] UpdateStream';
export const DELETE_STREAM = '[Stream] DeleteStream';

export type StreamActions =
    | CreateStreamAction
    | EditStreamAction
    | ResetStreamAction
    | UpdateStreamAction
    | DeleteStreamAction

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

export class DeleteStreamAction implements Action {
    readonly type = '[Stream] DeleteStream';

    constructor(readonly id: string) {

    }
}
