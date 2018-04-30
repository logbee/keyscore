import {Action} from "@ngrx/store";
import {AgentModel} from "./agents.model";

export const LOAD_AGENTS = '[Agents] Load';
export const LOAD_AGENTS_SUCCESS = '[Agents] LoadSuccess';
export const LOAD_AGENTS_FAILURE = '[Agents] LoadFailure';
export const INSPECT_AGENT = '[Agents] InspectAgent';
export const REMOVE_AGENT = '[Agents] RemoveAgent';

export type AgentsActions =
    | LoadAgentsAction
    | LoadAgentsSuccessAction
    | LoadAgentsFailureAction
    | InspectAgentAction
    | RemoveCurrentAgentAction

export class LoadAgentsAction implements Action {
    readonly type = LOAD_AGENTS;
}

export class LoadAgentsSuccessAction implements Action {
    readonly type = LOAD_AGENTS_SUCCESS;

    constructor(readonly agents: AgentModel[]) {
    }
}

export class LoadAgentsFailureAction implements Action {
    readonly type = LOAD_AGENTS_FAILURE;
    constructor(readonly cause: any) { }
}

export class InspectAgentAction implements Action  {
    readonly type = INSPECT_AGENT;

    constructor(readonly id: string) {

    }
}

export class RemoveCurrentAgentAction implements Action {
    readonly type = REMOVE_AGENT;
}