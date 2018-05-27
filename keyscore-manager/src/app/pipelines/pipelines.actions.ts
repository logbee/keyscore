import {Action} from "@ngrx/store";
import {FilterDescriptor, FilterModel, PipelineModel} from "./pipelines.model";

export const CREATE_PIPELINE = '[Pipeline] CreatePipeline';
export const EDIT_PIPELINE = '[Pipeline] EditPipeline';
export const LOCK_EDITING_PIPELINE = '[Pipeline] LockEditingPipeline';
export const RESET_PIPELINE = '[Pipeline] ResetPipeline';
export const UPDATE_PIPELINE = '[Pipeline] UpdatePipeline';
export const UPDATE_PIPELINE_SUCCESS = '[Pipeline] UpdatePipelineSuccess';
export const UPDATE_PIPELINE_FAILURE = '[Pipeline] UpdatePipelineFailure';
export const DELETE_PIPELINE = '[Pipeline] DeletePipeline';
export const DELETE_PIPELINE_SUCCESS = '[Pipeline] DeletePipelineSuccess';
export const DELETE_PIPELINE_FAILURE = '[Pipeline] DeletePipelineFailure';
export const ADD_FILTER = '[Pipeline] AddFilter';
export const MOVE_FILTER = '[Pipeline] MoveFilter';
export const REMOVE_FILTER = '[Pipeline] RemoveFilter';
export const UPDATE_FILTER = '[Pipeline] UpdateFilter';
export const LOAD_FILTER_DESCRIPTORS_SUCCESS = '[Pipeline] LoadFilterDescriptorsSuccess';
export const LOAD_FILTER_DESCRIPTORS_FAILURE = '[Pipeline] LoadFilterDescriptorsFailed';
export const LOAD_FILTER_DESCRIPTORS = '[Pipeline] LoadFilterDescriptors';


export type PipelineActions =
    | CreatePipelineAction
    | EditPipelineAction
    | ResetPipelineAction
    | UpdatePipelineAction
    | UpdatePipelineSuccessAction
    | UpdatePipelineFailureAction
    | DeletePipelineAction
    | AddFilterAction
    | MoveFilterAction
    | UpdateFilterAction
    | LoadFilterDescriptorsSuccessAction
    | LoadFilterDescriptorsFailureAction
    | LoadFilterDescriptorsAction
    | RemoveFilterAction
    | LockEditingPipelineAction
    | DeletePipelineSuccessAction
    | DeletePipelineFailureAction

export class CreatePipelineAction implements Action {
    readonly type = CREATE_PIPELINE;

    constructor(readonly id: string, readonly name: string, readonly description: string) {

    }
}

export class EditPipelineAction implements Action {
    readonly type = EDIT_PIPELINE;

    constructor(readonly id: string) {

    }
}

export class LockEditingPipelineAction implements Action {
    readonly type = LOCK_EDITING_PIPELINE;

    constructor(readonly isLocked: boolean) {
    }
}

export class ResetPipelineAction implements Action {
    readonly type = RESET_PIPELINE;

    constructor(readonly id: string) {

    }
}

export class UpdatePipelineAction implements Action {
    readonly type = UPDATE_PIPELINE;

    constructor(readonly pipeline: PipelineModel) {

    }
}

export class UpdatePipelineSuccessAction implements Action {
    readonly type = UPDATE_PIPELINE_SUCCESS;

    constructor(readonly pipeline: PipelineModel) {

    }
}

export class UpdatePipelineFailureAction implements Action {
    readonly type = UPDATE_PIPELINE_FAILURE;

    constructor(readonly pipeline: PipelineModel) {

    }
}

export class DeletePipelineAction implements Action {
    readonly type = DELETE_PIPELINE;

    constructor(readonly id: string) {

    }
}

export class DeletePipelineSuccessAction implements Action {
    readonly type = DELETE_PIPELINE_SUCCESS;

    constructor(readonly id: string) {

    }
}

export class DeletePipelineFailureAction implements Action {
    readonly type = DELETE_PIPELINE_FAILURE;

    constructor(readonly cause: any,readonly id:string) {

    }
}

export class AddFilterAction implements Action {
    readonly type = ADD_FILTER;

    constructor(readonly filter: FilterDescriptor) {

    }
}

export class MoveFilterAction implements Action {
    readonly type = MOVE_FILTER;

    constructor(readonly filterId: string, readonly position: number) {

    }
}

export class UpdateFilterAction implements Action {
    readonly type = UPDATE_FILTER;

    constructor(readonly filter: FilterModel, readonly values: any) {
    }
}

export class RemoveFilterAction implements Action {
    readonly type = REMOVE_FILTER;

    constructor(readonly filterId: string) {

    }
}


export class LoadFilterDescriptorsAction implements Action {
    readonly type = LOAD_FILTER_DESCRIPTORS

}

export class LoadFilterDescriptorsSuccessAction implements Action {
    readonly type = LOAD_FILTER_DESCRIPTORS_SUCCESS;

    constructor(readonly descriptors: FilterDescriptor[]) {
    }
}

export class LoadFilterDescriptorsFailureAction implements Action {
    readonly type = LOAD_FILTER_DESCRIPTORS_FAILURE;

    constructor(readonly cause: any) {
    }
}
