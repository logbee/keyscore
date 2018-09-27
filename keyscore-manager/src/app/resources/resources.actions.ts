import {Action} from "@ngrx/store";
import {Blueprint} from "../models/blueprints/Blueprint";
import {Descriptor} from "../models/descriptors/Descriptor";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {Configuration} from "../models/common/Configuration";

export const LOAD_ALL_BLUEPRINTS = "[Resources]GetAllBluePrintsAction";
export const LOAD_ALL_BLUEPRINTS_SUCCESS = "[Resources]LoadAllBlueprintsSuccess";
export const LOAD_ALL_BLUEPRINTS_FAILURE = "[Resources]LoadAllBlueprintsFailure";
export const LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT = "[Resources]LoadAllDescriptorsForBlueprint";
export const LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_SUCCESS = "[Resources]LoadAllDescriptorsForBlueprintSuccess";
export const LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_FAILURE = "[Resources]LoadAllDescriptorsForBlueprintFailure";
export const RESOLVED_ALL_DESCRIPTORS_SUCCESS = "[Resources]ResolvedAllDescriptorsSuccess";
export const LOAD_CONFIGURATIONS_SUCCESS = "[Resources]LoadConfigurationsSuccess";
export const LOAD_CONFIGURATIONS_FAILURE = "[Resources]LoadConfigurationsFailure";
export const STORE_DESCRIPTOR_REF = "[Resources]StoreDescriptorRef";
export const STORE_CONFIGURATION_REF = "[Resources]StoreConfigurationRef";

export type ResourcesActions =
    | LoadAllBlueprintsActions
    | LoadAllBlueprintsActionSuccess
    | LoadAllBlueprintsActionFailure
    | LoadAllDescriptorsForBlueprintAction
    | LoadAllDescriptorsForBlueprintSuccessAction
    | LoadAllDescriptorsForBlueprintFailureAction
    | ResolvedAllDescriptorsSuccessAction
    | LoadConfigurationsSuccessAction
    | LoadConfigurationsFailureAction
    | StoreConfigurationRefAction
    | StoreDescriptorRefAction;

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

    constructor(readonly cause: any) {

    }
}

export class ResolvedAllDescriptorsSuccessAction implements Action {
    public readonly type = RESOLVED_ALL_DESCRIPTORS_SUCCESS;

    constructor(readonly resolvedDescriptors: ResolvedFilterDescriptor[]) {

    }
}

export class LoadConfigurationsSuccessAction implements Action {
    public readonly type = LOAD_CONFIGURATIONS_SUCCESS;

    constructor (readonly configurations: Configuration[]) {

    }
}

export class LoadConfigurationsFailureAction implements Action {
    public readonly type = LOAD_CONFIGURATIONS_FAILURE;

    constructor(readonly cause: any) {

    }
}

export class StoreDescriptorRefAction  implements Action {
    public readonly type = STORE_DESCRIPTOR_REF;

    constructor(readonly uuid: string) {
    }
}

export class StoreConfigurationRefAction implements Action {
    public readonly type = STORE_CONFIGURATION_REF;

    constructor(readonly uuid: string) {

    }
}

