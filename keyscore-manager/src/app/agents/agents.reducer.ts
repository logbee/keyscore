import {AgentsActions, LOAD_AGENTS, LOAD_AGENTS_FAILURE, LOAD_AGENTS_SUCCESS} from "./agents.actions";
import {AgentsState} from "./agents.model";


const initialState: AgentsState = {
    agents: []
};

export function AgentsReducer(state: AgentsState = initialState, action: AgentsActions): AgentsState {

    const result: AgentsState = Object.assign({}, state);

    switch (action.type) {
        case LOAD_AGENTS_SUCCESS:
            result.agents = action.agents;
            break;

        case LOAD_AGENTS:
        case LOAD_AGENTS_FAILURE:
            result.agents = [];
            break;
    }

    return result;
}
