import {Action} from "@ngrx/store";

export const ERROR_ACTION = "[Error]ErrorAction";

export class ErrorAction implements  Action {
    public readonly type = ERROR_ACTION;

    constructor(readonly httpError: string, readonly message: string) {

    }
}

export type ErrorActions = ErrorAction;