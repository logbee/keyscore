import {Action} from "@ngrx/store";
import {Blueprint} from "../../../modules/keyscore-manager-models/src/main/blueprints/Blueprint";
import {Descriptor} from "../../../modules/keyscore-manager-models/src/main/descriptors/Descriptor";
import {ResolvedFilterDescriptor} from "../../../modules/keyscore-manager-models/src/main/descriptors/FilterDescriptor";
import {Configuration} from "../../../modules/keyscore-manager-models/src/main/common/Configuration";
import {Health} from "../../../modules/keyscore-manager-models/src/main/common/Health";
import {ResourceInstanceState} from "../../../modules/keyscore-manager-models/src/main/filter-model/ResourceInstanceState";

export const LOAD_ALL_BLUEPRINTS = "[Resources]GetAllBluePrintsAction";
export const LOAD_ALL_BLUEPRINTS_SUCCESS = "[Resources]LoadAllBlueprintsSuccess";
export const LOAD_ALL_BLUEPRINTS_FAILURE = "[Resources]LoadAllBlueprintsFailure";
export const LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT = "[Resources]LoadAllDescriptorsForBlueprint";
export const LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_SUCCESS = "[Resources]LoadAllDescriptorsForBlueprintSuccess";
export const LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_FAILURE = "[Resources]LoadAllDescriptorsForBlueprintFailure";
export const RESOLVED_ALL_DESCRIPTORS_SUCCESS = "[Resources]ResolvedAllDescriptorsSuccess";
export const LOAD_CONFIGURATIONS_SUCCESS = "[Resources]LoadConfigurationsSuccess";
export const LOAD_CONFIGURATIONS_FAILURE = "[Resources]LoadConfigurationsFailure";
export const STORE_BLUEPRINT_REF = "[Resources]StoreBlueprintRefAction";
export const STORE_DESCRIPTOR_REF = "[Resources]StoreDescriptorRef";
export const STORE_CONFIGURATION_REF = "[Resources]StoreConfigurationRef";
export const GET_RESOURCE_STATE = "[Resources]GetResourceStateAction";
export const GET_RESOURCE_STATE_SUCCESS = "[Resources]GetResourceStateSucces";
export const GET_RESOURCE_STATE_FAILURE = "[Resources]GetResourceStateFailure";

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
    | StoreDescriptorRefAction
    | StoreBlueprintRefAction
    | GetResourceStateAction
    | GetResourceStateSuccess
    | GetResourceStateFailure;

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

export class StoreBlueprintRefAction implements Action {
        public readonly  type = STORE_BLUEPRINT_REF;

    constructor(readonly uuid: string) {

    }
}

export class GetResourceStateAction implements Action {
    public readonly type = GET_RESOURCE_STATE;

    constructor(readonly resourceId: string) {

    }
}

export class GetResourceStateSuccess implements  Action {
    public readonly type = GET_RESOURCE_STATE_SUCCESS;

    constructor(readonly resourceId: string, readonly instance: ResourceInstanceState) {

    }
}

export class GetResourceStateFailure implements  Action {
    public readonly type = GET_RESOURCE_STATE_FAILURE;

    constructor(readonly cause: any) {

    }
}