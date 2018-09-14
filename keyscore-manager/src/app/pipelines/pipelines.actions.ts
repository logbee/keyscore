import {Action} from "@ngrx/store";
import {PipelineConfiguration} from "../models/pipeline-model/PipelineConfiguration";
import {InternalPipelineConfiguration} from "../models/pipeline-model/InternalPipelineConfiguration";
import {PipelineInstance} from "../models/pipeline-model/PipelineInstance";
import {FilterDescriptor} from "../models/filter-model/FilterDescriptor";
import {FilterConfiguration} from "../models/filter-model/FilterConfiguration";

export const CREATE_PIPELINE = "[Pipeline] CreatePipeline";
export const EDIT_PIPELINE = "[Pipeline] EditPipeline";
export const EDIT_PIPELINE_SUCCESS = "[Pipeline] EditPipelineSuccess";
export const EDIT_PIPELINE_FAILURE = "[Pipeline] EditPipelineFailure";
export const LOCK_EDITING_PIPELINE = "[Pipeline] LockEditingPipeline";
export const RESET_PIPELINE = "[Pipeline] ResetPipeline";
export const UPDATE_PIPELINE = "[Pipeline] UpdatePipeline";
export const UPDATE_PIPELINE_BLOCKLY = "[Pipeline] UpdatePipelineBlockly";
export const UPDATE_PIPELINE_SUCCESS = "[Pipeline] UpdatePipelineSuccess";
export const UPDATE_PIPELINE_FAILURE = "[Pipeline] UpdatePipelineFailure";
export const DELETE_PIPELINE = "[Pipeline] DeletePipeline";
export const DELETE_PIPELINE_SUCCESS = "[Pipeline] DeletePipelineSuccess";
export const DELETE_PIPELINE_FAILURE = "[Pipeline] DeletePipelineFailure";
export const LOAD_ALL_PIPELINES = "[Pipeline] LoadAllPipelines";
export const LOAD_ALL_PIPELINES_SUCCESS = "[Pipeline] LoadAllPipelinesSuccess";
export const LOAD_ALL_PIPELINES_FAILURE = "[Pipeline] LoadAllPipelinesFailure";
export const UPDATE_PIPELINE_POLLING = "[Pipeline] UpdatePipelinePolling";
export const ADD_FILTER = "[Pipeline] AddFilter";
export const MOVE_FILTER = "[Pipeline] MoveFilter";
export const REMOVE_FILTER = "[Pipeline] RemoveFilter";
export const UPDATE_FILTER = "[Pipeline] UpdateFilter";
export const LOAD_FILTER_DESCRIPTORS_SUCCESS = "[Pipeline] LoadFilterDescriptorsSuccess";
export const LOAD_FILTER_DESCRIPTORS_FAILURE = "[Pipeline] LoadFilterDescriptorsFailed";
export const LOAD_FILTER_DESCRIPTORS = "[Pipeline] LoadFilterDescriptors";

export type PipelineActions =
    | CreatePipelineAction
    | EditPipelineAction
    | EditPipelineSuccessAction
    | EditPipelineFailureAction
    | ResetPipelineAction
    | UpdatePipelineAction
    | UpdatePipelineSuccessAction
    | UpdatePipelineFailureAction
    | DeletePipelineAction
    | LoadAllPipelinesAction
    | LoadAllPipelinesSuccessAction
    | LoadAllPipelinesFailureAction
    | UpdatePipelinePollingAction
    | AddFilterAction
    | MoveFilterAction
    | UpdateFilterAction
    | LoadFilterDescriptorsSuccessAction
    | LoadFilterDescriptorsFailureAction
    | LoadFilterDescriptorsAction
    | RemoveFilterAction
    | LockEditingPipelineAction
    | DeletePipelineSuccessAction
    | DeletePipelineFailureAction;

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

    constructor(readonly pipelineConfiguration: PipelineConfiguration) {

    }
}

export class EditPipelineFailureAction implements Action {
    public readonly type = EDIT_PIPELINE_FAILURE;

    constructor(readonly id: string, readonly cause: any) {

    }
}

export class LockEditingPipelineAction implements Action {
    public readonly type = LOCK_EDITING_PIPELINE;

    constructor(readonly isLocked: boolean) {
    }
}

export class ResetPipelineAction implements Action {
    public readonly type = RESET_PIPELINE;

    constructor(readonly id: string) {

    }
}

export class UpdatePipelineAction implements Action {
    public readonly type = UPDATE_PIPELINE;

    constructor(readonly pipeline: InternalPipelineConfiguration) {

    }
}

export class UpdatePipelineWithBlocklyAction implements Action {
    public readonly type = UPDATE_PIPELINE_BLOCKLY;

    constructor(readonly pipelineConfiguration: PipelineConfiguration) {

    }
}

export class UpdatePipelineSuccessAction implements Action {
    public readonly type = UPDATE_PIPELINE_SUCCESS;

    constructor(readonly pipeline: InternalPipelineConfiguration) {

    }
}

export class UpdatePipelineFailureAction implements Action {
    public readonly type = UPDATE_PIPELINE_FAILURE;

    constructor(readonly cause: any, readonly pipeline: InternalPipelineConfiguration) {

    }
}

export class LoadAllPipelinesAction implements Action {
    public readonly type = LOAD_ALL_PIPELINES;
}

export class LoadAllPipelinesSuccessAction implements Action {
    public readonly type = LOAD_ALL_PIPELINES_SUCCESS;

    constructor(readonly pipelineInstances: PipelineInstance[]) {

    }
}

export class LoadAllPipelinesFailureAction implements Action {
    public readonly type = LOAD_ALL_PIPELINES_FAILURE;

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

export class AddFilterAction implements Action {
    public readonly type = ADD_FILTER;

    constructor(readonly filter: ResolvedFilterDescriptor) {

    }
}

export class MoveFilterAction implements Action {
    public readonly type = MOVE_FILTER;

    constructor(readonly filterId: string, readonly position: number) {

    }
}

export class UpdateFilterAction implements Action {
    public readonly type = UPDATE_FILTER;

    constructor(readonly filter: FilterConfiguration, readonly values: any) {
    }
}

export class RemoveFilterAction implements Action {
    public readonly type = REMOVE_FILTER;

    constructor(readonly filterId: string) {

    }
}

export class LoadFilterDescriptorsAction implements Action {
    public readonly type = LOAD_FILTER_DESCRIPTORS;

}

export class LoadFilterDescriptorsSuccessAction implements Action {
    public readonly type = LOAD_FILTER_DESCRIPTORS_SUCCESS;

    constructor(readonly descriptors: FilterDescriptor[]) {
    }
}

export class LoadFilterDescriptorsFailureAction implements Action {
    public readonly type = LOAD_FILTER_DESCRIPTORS_FAILURE;

    constructor(readonly cause: any) {
    }
}
