import {Action} from "@ngrx/store";

export const CREATE_NEW_STREAM = '[Stream] CreateNewStream';

export class CreateStreamAction implements Action {
    readonly type = '[Stream] CreateNewStream';

    constructor(readonly id: string, readonly name: string, readonly description: string) {
    }
}

export type StreamActions =
    | CreateStreamAction