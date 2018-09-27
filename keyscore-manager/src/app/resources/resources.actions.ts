import {Action} from "@ngrx/store";
import {Blueprint} from "../models/blueprints/Blueprint";
import {Descriptor} from "../models/descriptors/Descriptor";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";

export const LOAD_ALL_BLUEPRINTS = "[Resources]GetAllBluePrintsAction";
export const LOAD_ALL_BLUEPRINTS_SUCCESS = "[Resources]LoadAllBlueprintsSuccess";
export const LOAD_ALL_BLUEPRINTS_FAILURE = "[Resources]LoadAllBlueprintsFailure";
export const LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT = "[Resources]LoadAllDescriptorsForBlueprint";
export const LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_SUCCESS = "[Resources]LoadAllDescriptorsForBlueprintSuccess";
export const LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_FAILURE = "[Resources]LoadAllDescriptorsForBlueprintFailure";
export const RESOLVED_ALL_DESCRIPTORS_SUCCESS = "[Resources]ResolvedAllDescriptorsSuccess";

export type ResourcesActions =
    | LoadAllBlueprintsActions
    | LoadAllBlueprintsActionSuccess
    | LoadAllBlueprintsActionFailure
    | LoadAllDescriptorsForBlueprintAction
    | LoadAllDescriptorsForBlueprintSuccessAction
    | LoadAllDescriptorsForBlueprintFailureAction
    | ResolvedAllDescriptorsSuccessAction;

export class LoadAllBlueprintsActions implements Action {
    public readonly type = LOAD_ALL_BLUEPRINTS;
}

export class LoadAllBlueprintsActionSuccess implements Action {
    public readonly type = LOAD_ALL_BLUEPRINTS_SUCCESS;

    constructor(readonly blueprints: Blueprint[]) {
    }
}

export class LoadAllBlueprintsActionFailure implements Action {
    public readonly type = LOAD_ALL_BLUEPRINTS_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class LoadAllDescriptorsForBlueprintAction implements Action {
    public readonly type = LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT;
}

export class LoadAllDescriptorsForBlueprintSuccessAction implements Action {
    public readonly type = LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_SUCCESS;

    constructor(readonly descriptors: Descriptor[]) {

    }
}

export class LoadAllDescriptorsForBlueprintFailureAction implements Action {
    public readonly type = LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_FAILURE;

    constructor(private readonly cause: any) {

    }
}

export class ResolvedAllDescriptorsSuccessAction implements Action {
    public readonly type = RESOLVED_ALL_DESCRIPTORS_SUCCESS;

    constructor(readonly resolvedDescriptors: ResolvedFilterDescriptor[]) {

    }
}


