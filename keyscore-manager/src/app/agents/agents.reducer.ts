import {
    AgentsActions,
    DELETE_AGENT_SUCCESS,
    INSPECT_AGENT,
    LOAD_AGENTS_FAILURE,
    LOAD_AGENTS_SUCCESS,
    REMOVE_AGENT
} from "./agents.actions";

import {AgentsState} from "./agents.model";

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
