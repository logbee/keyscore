import {Action} from "@ngrx/store";
import {Blueprint} from "../models/blueprints/Blueprint";

export const LOAD_ALL_BLUEPRINTS = "[RunningFilters]GetAllBluePrintsACtion";
export const LOAD_ALL_BLUEPRINTS_SUCCESS = "[RunningFilters]LoadAllBlueprintsSuccess";
export const LOAD_ALL_BLUEPRINTS_FAILURE = "[RunningFilters]LoadAllBlueprintsFailure";

export type ResourcesActions =
    | LoadAllBlueprintsActions
    | LoadAllBlueprintsActionsSuccess
    | LoadAllBlueprintsActionsFailure;

export class LoadAllBlueprintsActions implements Action {
    public readonly type = LOAD_ALL_BLUEPRINTS;
}

export class LoadAllBlueprintsActionsSuccess implements Action {
    public readonly type = LOAD_ALL_BLUEPRINTS_SUCCESS;

    constructor(readonly blueprints: Blueprint[]) {
    }
}

export class LoadAllBlueprintsActionsFailure implements Action {
    public readonly type = LOAD_ALL_BLUEPRINTS_FAILURE;

    constructor(readonly cause: any) {
    }
}

