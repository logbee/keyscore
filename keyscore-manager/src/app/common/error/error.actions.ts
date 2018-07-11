import {Action} from "@ngrx/store";

export const ERROR_EVENT = "[Error]ErrorEvent";

export class ErrorEvent implements  Action {
    public readonly type = ERROR_EVENT;

    constructor(readonly httpError:string, readonly message: string) {

    }
}

export type ErrorActions = ErrorEvent