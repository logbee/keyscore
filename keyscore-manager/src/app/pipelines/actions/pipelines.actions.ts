import {Action} from "@ngrx/store";
import {PipelineInstance} from "../../models/pipeline-model/PipelineInstance";
import {ResolvedFilterDescriptor} from "../../models/descriptors/FilterDescriptor";
import {Blueprint, PipelineBlueprint} from "../../models/blueprints/Blueprint";
import {Configuration} from "../../models/common/Configuration";
import {Descriptor} from "../../models/descriptors/Descriptor";
import {EditingPipelineModel} from "../../models/pipeline-model/EditingPipelineModel";
import {Ref} from "../../models/common/Ref";

export const CREATE_PIPELINE = "[Pipeline] CreatePipeline";

export const EDIT_PIPELINE = "[Pipeline] EditPipeline";
export const EDIT_PIPELINE_SUCCESS = "[Pipeline] EditPipelineSuccess";
export const EDIT_PIPELINE_FAILURE = "[Pipeline] EditPipelineFailure";

export const LOAD_EDIT_PIPELINE_BLUEPRINTS = "[Pipeline] LoadEditPipelineBlueprints";
export const LOAD_EDIT_PIPELINE_CONFIG = "[Pipeline] LoadEditPipelineConfig";

export const RESET_PIPELINE = "[Pipeline] ResetPipeline";

export const UPDATE_PIPELINE = "[Pipeline] UpdatePipeline";
export const UPDATE_PIPELINE_SUCCESS = "[Pipeline] UpdatePipelineSuccess";
export const UPDATE_PIPELINE_FAILURE = "[Pipeline] UpdatePipelineFailure";

export const RUN_PIPELINE = "[Pipeline] RunPipeline";
export const RUN_PIPELINE_SUCCESS = "[Pipeline] RunPipelineSuccess";
export const RUN_PIPELINE_FAILURE = "[Pipeline] RunPipelineFailure";

export const DELETE_PIPELINE = "[Pipeline] DeletePipeline";
export const DELETE_PIPELINE_SUCCESS = "[Pipeline] DeletePipelineSuccess";
export const DELETE_PIPELINE_FAILURE = "[Pipeline] DeletePipelineFailure";

export const LOAD_ALL_PIPELINE_INSTANCES = "[Pipeline] LoadAllPipelines";
export const LOAD_ALL_PIPELINE_INSTANCES_SUCCESS = "[Pipeline] LoadAllPipelinesSuccess";
export const LOAD_ALL_PIPELINE_INSTANCES_FAILURE = "[Pipeline] LoadAllPipelinesFailure";

export const LOAD_PIPELINEBLUEPRINTS_SUCCESS = "[Pipeline] LoadAllPipelineBlueprintsSuccess";
export const LOAD_PIPELINEBLUEPRINTS_FAILURE = "[Pipeline] LoadAllPipelineBlueprintsFailure";
export const LOAD_PIPELINEBLUEPRINTS = "[Pipeline] LoadAllPipelineBlueprints";

export const UPDATE_PIPELINE_POLLING = "[Pipeline] UpdatePipelinePolling";

export const LOAD_FILTER_DESCRIPTORS_SUCCESS = "[Pipeline] LoadFilterDescriptorsSuccess";
export const LOAD_FILTER_DESCRIPTORS_FAILURE = "[Pipeline] LoadFilterDescriptorsFailed";
export const LOAD_FILTER_DESCRIPTORS = "[Pipeline] LoadFilterDescriptors";
export const RESOLVE_FILTER_DESCRIPTORS_SUCCESS = "[Pipeline] ResolveFilterDescriptorsSuccess";
export const TRIGGER_FILTER_RESET = "[Pipeline] TriggerFilterReset";
export const CONFIGS_FOR_BLUEPRINT = "[Pipeline] ConfigurationsForBlueprint";

export const CHECK_IS_PIPELINE_RUNNING = "[Pipeline] CheckIsPipelineRunning";

export type PipelineActions =
    | CreatePipelineAction
    | EditPipelineAction
    | EditPipelineSuccessAction
    | EditPipelineFailureAction
    | LoadEditBlueprintsAction
    | LoadEditPipelineConfigAction
    | ResetPipelineAction
    | UpdatePipelineAction
    | UpdatePipelineSuccessAction
    | UpdatePipelineFailureAction
    | RunPipelineAction
    | RunPipelineSuccessAction
    | RunPipelineFailureAction
    | DeletePipelineAction
    | LoadAllPipelineInstancesAction
    | LoadAllPipelineInstancesSuccessAction
    | LoadAllPipelineInstancesFailureAction
    | LoadPipelineBlueprints
    | LoadPipelineBlueprintsSuccess
    | LoadPipelineBlueprintsFailure
    | UpdatePipelinePollingAction
    | LoadFilterDescriptorsSuccessAction
    | LoadFilterDescriptorsFailureAction
    | ResolveFilterDescriptorSuccessAction
    | LoadFilterDescriptorsAction
    | DeletePipelineSuccessAction
    | DeletePipelineFailureAction
    | TriggerFilterResetAction
    | ConfigurationsForBlueprintId;

export class CreatePipelineAction implements Action {
    public readonly type = CREATE_PIPELINE;

    constructor(readonly id: string, readonly name: string, readonly description: string) {

    }
}

export class EditPipelineAction implements Action {
    public readonly type = EDIT_PIPELINE;

    constructor(readonly id: string) {

    }
}

export class EditPipelineSuccessAction implements Action {
    public readonly type = EDIT_PIPELINE_SUCCESS;

    constructor(readonly pipelineBlueprint: PipelineBlueprint,
                readonly blueprints: Blueprint[],
                readonly configurations: Configuration[]) {

    }
}

export class EditPipelineFailureAction implements Action {
    public readonly type = EDIT_PIPELINE_FAILURE;

    constructor(readonly cause: any) {

    }
}

export class LoadEditBlueprintsAction implements Action {
    public readonly type = LOAD_EDIT_PIPELINE_BLUEPRINTS;

    constructor(readonly pipelineBlueprint: PipelineBlueprint) {

    }
}

export class LoadEditPipelineConfigAction implements Action {
    public readonly type = LOAD_EDIT_PIPELINE_CONFIG;

    constructor(readonly pipelineBlueprint: PipelineBlueprint,
                readonly blueprints: Blueprint[]) {

    }
}

export class ResetPipelineAction implements Action {
    public readonly type = RESET_PIPELINE;

    constructor(readonly id: string) {

    }
}

export class UpdatePipelineAction implements Action {
    public readonly type = UPDATE_PIPELINE;

    constructor(readonly pipeline: EditingPipelineModel, readonly runAfterUpdate: boolean = false) {

    }
}

export class UpdatePipelineSuccessAction implements Action {
    public readonly type = UPDATE_PIPELINE_SUCCESS;

    constructor(readonly pipeline: EditingPipelineModel, readonly runPipeline: boolean) {

    }
}

export class UpdatePipelineFailureAction implements Action {
    public readonly type = UPDATE_PIPELINE_FAILURE;

    constructor(readonly cause: any, readonly pipeline: EditingPipelineModel) {

    }
}

export class RunPipelineAction implements Action {
    public readonly type = RUN_PIPELINE;

    constructor(readonly blueprintRef: Ref) {

    }
}

export class RunPipelineSuccessAction implements Action {
    public readonly type = RUN_PIPELINE_SUCCESS;

    constructor(readonly blueprintRef: Ref) {

    }
}

export class RunPipelineFailureAction implements Action {
    public readonly type = RUN_PIPELINE_FAILURE;

    constructor(readonly cause: any, readonly blueprintRef: Ref) {

    }
}

export class LoadAllPipelineInstancesAction implements Action {
    public readonly type = LOAD_ALL_PIPELINE_INSTANCES;

}

export class LoadAllPipelineInstancesSuccessAction implements Action {
    public readonly type = LOAD_ALL_PIPELINE_INSTANCES_SUCCESS;

    constructor(readonly pipelineInstances: PipelineInstance[]) {

    }
}

export class LoadAllPipelineInstancesFailureAction implements Action {
    public readonly type = LOAD_ALL_PIPELINE_INSTANCES_FAILURE;

    constructor(readonly cause: any) {

    }
}

export class UpdatePipelinePollingAction implements Action {
    public readonly type = UPDATE_PIPELINE_POLLING;

    constructor(readonly isPolling: boolean) {

    }
}

export class DeletePipelineAction implements Action {
    public readonly type = DELETE_PIPELINE;

    constructor(readonly id: string) {

    }
}

export class DeletePipelineSuccessAction implements Action {
    public readonly type = DELETE_PIPELINE_SUCCESS;

    constructor(readonly id: string) {

    }
}

export class DeletePipelineFailureAction implements Action {
    public readonly type = DELETE_PIPELINE_FAILURE;

    constructor(readonly cause: any, readonly id: string) {

    }
}

export class LoadFilterDescriptorsAction implements Action {
    public readonly type = LOAD_FILTER_DESCRIPTORS;

}

export class LoadFilterDescriptorsSuccessAction implements Action {
    public readonly type = LOAD_FILTER_DESCRIPTORS_SUCCESS;

    constructor(readonly descriptors: Descriptor[]) {
    }
}

export class LoadFilterDescriptorsFailureAction implements Action {
    public readonly type = LOAD_FILTER_DESCRIPTORS_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class ResolveFilterDescriptorSuccessAction implements Action {
    public readonly type = RESOLVE_FILTER_DESCRIPTORS_SUCCESS;

    constructor(readonly resolvedDescriptors: ResolvedFilterDescriptor[]) {

    }
}

export class LoadPipelineBlueprintsSuccess implements Action {
    public readonly type = LOAD_PIPELINEBLUEPRINTS_SUCCESS;

    constructor(readonly pipelineBlueprints: PipelineBlueprint[]) {

    }
}

export class LoadPipelineBlueprintsFailure implements Action {
    public readonly type = LOAD_PIPELINEBLUEPRINTS_FAILURE;

    constructor(readonly cause: any) {

    }
}

export class LoadPipelineBlueprints implements Action {
    public readonly type = LOAD_PIPELINEBLUEPRINTS;
}


export class TriggerFilterResetAction implements Action {
    public readonly type  = TRIGGER_FILTER_RESET;

    constructor(readonly uuid: string) {
    }
}

export class TriggerFilterResetFailure implements Action {
    public readonly type = TRIGGER_FILTER_RESET;

    constructor(readonly cause: any) {

    }
}

export class ConfigurationsForBlueprintId implements Action {
    public readonly type = CONFIGS_FOR_BLUEPRINT;

    constructor(readonly blueprints: Ref[]) {

    }
}

export class CheckIsPipelineRunning implements Action {
    public readonly type = CHECK_IS_PIPELINE_RUNNING;
    constructor(readonly pipelineRef: Ref,readonly timeToLive:number){

    }
}


