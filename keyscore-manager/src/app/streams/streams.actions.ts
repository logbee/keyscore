import {Action} from "@ngrx/store";

export const CREATE_STREAM = '[Stream] CreateStream';
export const EDIT_STREAM = '[Stream] EditStream';

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

export type StreamActions =
    | CreateStreamAction
    | EditStreamAction