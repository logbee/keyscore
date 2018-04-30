import {createFeatureSelector, createSelector} from "@ngrx/store";

export interface AgentsState {
    agents: AgentModel[]
    currentAgentid: string
}

export interface AgentModel {
    id: string
    name: string
    host: string
}

export const getAgentState = createFeatureSelector<AgentsState>('agents');

export const getAgents = createSelector(getAgentState, (state: AgentsState) => state.agents);


export const getCurrentAgent = createSelector(
    getAgentState,
    (state: AgentsState) => state.agents.filter(agent => agent.id === state.currentAgentid)[0]);

