import {Action} from "@ngrx/store";

export const ERROR_ACTION = "[Error]ErrorAction";
export const ERROR_RESET_ACTION = "[Error]ErrorResetAction";

export class ErrorAction implements Action {
    public readonly type = ERROR_ACTION;

    constructor(readonly httpError: string, readonly message: string) {

    }
}

export class ResetErrorAction implements Action {
    public readonly type = ERROR_RESET_ACTION;

    constructor() {
    }
}

export type ErrorActions =
    | ErrorAction
    | ResetErrorAction;