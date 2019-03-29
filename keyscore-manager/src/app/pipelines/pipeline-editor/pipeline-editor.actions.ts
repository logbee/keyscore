import {Action} from "@ngrx/store";
import {FilterDescriptor} from "../../../../modules/keyscore-manager-models/src/main/descriptors/FilterDescriptor";

export const REMOVE_FILTER = "[Pipelines] REMOVE_FILTER";
export const MOVE_FILTER = "[Pipelines] MOVE_FILTER";
export const EDIT_FILTER = "[Pipelines] EDIT_FILTER";
export const SAVE_FILTER = "[Pipelines] SAVE_FILTER";
export const ENABLE_FILTER = "[Pipelines] ENABLE_FILTER";
export const DISABLE_FILTER = "[Pipelines] DISABLE_FILTER";
export const ADD_FILTER = "[Pipelines] ADD_FILTER";

export type PipelineEditorActions =
    | RemoveFilterAction
    | MoveFilterAction
    | EditFilterAction
    | SaveFilterAction
    | EnableFilterAction
    | DisableFilterAction
    | AddFilterAction;

export class RemoveFilterAction implements Action {
    public readonly type = "[Pipelines] REMOVE_FILTER";

    constructor(public filterId: number) {
    }
}

export class MoveFilterAction implements Action {
    public readonly type = "[Pipelines] MOVE_FILTER";

    constructor(public filterId: number, public position: number) {
    }
}

export class EditFilterAction implements Action {
    public readonly type = "[Pipelines] EDIT_FILTER";

    constructor(public filterId: number) {
    }
}

export class SaveFilterAction implements Action {
    public readonly type = "[Pipelines] SAVE_FILTER";

    constructor(public filterId: number) {
    }
}

export class EnableFilterAction implements Action {
    public readonly type = "[Pipelines] ENABLE_FILTER";

    constructor(public filterId: number) {
    }
}

export class DisableFilterAction implements Action {
    public readonly type = "[Pipelines] DISABLE_FILTER";

    constructor(public filterId: number) {
    }
}

export class AddFilterAction implements Action {
    public readonly type = "[Pipelines] ADD_FILTER";

    constructor(readonly filter: FilterDescriptor) {
    }
}
