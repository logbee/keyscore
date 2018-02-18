import {createFeatureSelector, createSelector} from "@ngrx/store";

export interface AgentsState {
    agents: AgentModel[]
}

export interface AgentModel {
    uid: string
    name: string
    host: string
}

export const getAgentState = createFeatureSelector<AgentsState>('agents');

export const getAgents = createSelector(getAgentState, (state: AgentsState) => state.agents);