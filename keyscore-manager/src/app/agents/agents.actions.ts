import {Action} from "@ngrx/store";
import {AgentModel} from "./agents.model";

export const LOAD_AGENTS = "[Agents] Load";
export const LOAD_AGENTS_SUCCESS = "[Agents] LoadSuccess";
export const LOAD_AGENTS_FAILURE = "[Agents] LoadFailure";
export const INSPECT_AGENT = "[Agents] InspectAgent";
export const REMOVE_AGENT = "[Agents] RemoveAgent";
export const DELETE_AGENT_SUCCESS = "[Agent] DeleteAgentSuccess";
export const DELETE_AGENT_FAILURE = "[Agent] DeleteAgentFailure";

export type AgentsActions =
    | LoadAgentsAction
    | LoadAgentsSuccessAction
    | LoadAgentsFailureAction
    | InspectAgentAction
    | RemoveCurrentAgentAction
    | DeleteAgentSuccessAction
    | DeleteAgentFailureAction;

export class LoadAgentsAction implements Action {
    public readonly type = LOAD_AGENTS;
}

export class LoadAgentsSuccessAction implements Action {
    public readonly type = LOAD_AGENTS_SUCCESS;

    constructor(readonly agents: AgentModel[]) {
    }
}

export class LoadAgentsFailureAction implements Action {
    public readonly type = LOAD_AGENTS_FAILURE;
    constructor(readonly cause: any) { }
}

export class InspectAgentAction implements Action  {
    public readonly type = INSPECT_AGENT;

    constructor(readonly id: string) {

    }
}

export class RemoveCurrentAgentAction implements Action {
    public readonly type = REMOVE_AGENT;

    constructor(readonly id: string) {

    }
}

export class DeleteAgentSuccessAction implements Action {
    public readonly type = DELETE_AGENT_SUCCESS;

}

export class DeleteAgentFailureAction implements Action {
    public readonly type = DELETE_AGENT_FAILURE;

    constructor(readonly cause: any) {

    }
}


