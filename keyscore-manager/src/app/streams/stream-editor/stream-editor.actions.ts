import {Action} from "@ngrx/store";
import {FilterDescriptor} from "../streams.model";

export const REMOVE_FILTER = '[Streams] REMOVE_FILTER';
export const MOVE_FILTER = '[Streams] MOVE_FILTER';
export const EDIT_FILTER = '[Streams] EDIT_FILTER';
export const SAVE_FILTER = '[Streams] SAVE_FILTER';
export const ENABLE_FILTER = '[Streams] ENABLE_FILTER';
export const DISABLE_FILTER = '[Streams] DISABLE_FILTER';
export const ADD_FILTER = '[Streams] ADD_FILTER';

export type StreamEditorActions =
    | RemoveFilterAction
    | MoveFilterAction
    | EditFilterAction
    | SaveFilterAction
    | EnableFilterAction
    | DisableFilterAction
    | AddFilterAction

export class RemoveFilterAction implements Action {
    readonly type = '[Streams] REMOVE_FILTER';

    constructor(public filterId: number) {
    }
}

export class MoveFilterAction implements Action {
    readonly type = '[Streams] MOVE_FILTER';

    constructor(public filterId: number, public position: number) {
    }
}

export class EditFilterAction implements Action {
    readonly type = '[Streams] EDIT_FILTER';

    constructor(public filterId: number) {
    }
}

export class SaveFilterAction implements Action {
    readonly type = '[Streams] SAVE_FILTER';

    constructor(public filterId: number) {
    }
}

export class EnableFilterAction implements Action {
    readonly type = '[Streams] ENABLE_FILTER';

    constructor(public filterId: number) {
    }
}

export class DisableFilterAction implements Action {
    readonly type = '[Streams] DISABLE_FILTER';

    constructor(public filterId: number) {
    }
}

export class AddFilterAction implements Action {
    readonly type = '[Streams] ADD_FILTER';

    constructor(readonly filter: FilterDescriptor) {
    }
}
