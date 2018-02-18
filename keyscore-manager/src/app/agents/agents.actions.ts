import {Action} from "@ngrx/store";
import {AgentModel} from "./agents.model";

export const LOAD_AGENTS = '[Agents] Load';
export const LOAD_AGENTS_SUCCESS = '[Agents] LoadSuccess';
export const LOAD_AGENTS_FAILURE = '[Agents] LoadFailure';

export type AgentsActions =
    | LoadAgentsAction
    | LoadAgentsSuccessAction
    | LoadAgentsFailureAction

export class LoadAgentsAction implements Action {
    readonly type = '[Agents] Load';
}

export class LoadAgentsSuccessAction implements Action {
    readonly type = '[Agents] LoadSuccess';

    constructor(readonly agents: AgentModel[]) {
    }
}

export class LoadAgentsFailureAction implements Action {
    readonly type = '[Agents] LoadFailure';
}