import {
    AgentsActions,
    DELETE_AGENT_SUCCESS,
    INSPECT_AGENT,
    LOAD_AGENTS_FAILURE,
    LOAD_AGENTS_SUCCESS
} from "./agents.actions";

import {createFeatureSelector, createSelector} from "@ngrx/store";
import {Agent} from "@/../modules/keyscore-manager-models/src/main/common/Agent";

export class AgentsState {
    agents: Agent[];
    currentAgentid: string;
}

const initialState: AgentsState = {
    agents: [],
    currentAgentid: ""
};

export function AgentsReducer(state: AgentsState = initialState, action: AgentsActions): AgentsState {

    const result: AgentsState = Object.assign({}, state);

    switch (action.type) {
        case LOAD_AGENTS_SUCCESS:
            result.agents = action.agents;
            break;

        case LOAD_AGENTS_FAILURE:
            result.agents = [];
            break;

        case INSPECT_AGENT:
            result.currentAgentid = action.id;
            break;

        case DELETE_AGENT_SUCCESS:
            result.currentAgentid = "";
    }

    return result;
}

export const getAgentState = createFeatureSelector<AgentsState>("agents");

export const getAgents = createSelector(getAgentState, (state: AgentsState) => state.agents);

export const getCurrentAgent = createSelector(getAgentState,
    (state: AgentsState) => state.agents.filter((agent) => agent.id === state.currentAgentid)[0]);

export const getCurrentAgentId = createSelector(getAgentState, (state: AgentsState) => state.currentAgentid);
