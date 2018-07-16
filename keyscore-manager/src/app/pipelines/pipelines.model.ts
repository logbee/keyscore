import {createFeatureSelector, createSelector} from "@ngrx/store";
import {PipelineInstance} from "../models/pipeline-model/PipelineInstance";
import {InternalPipelineConfiguration} from "../models/pipeline-model/InternalPipelineConfiguration";
import {FilterConfiguration} from "../models/filter-model/FilterConfiguration";
import {FilterDescriptor} from "../models/filter-model/FilterDescriptor";
import {FilterInstanceState} from "../models/filter-model/FilterInstanceState";
// export interface PipelineInstance {
//     id: string;
//     name: string;
//     description: string;
//     configurationId: string;
//     health: Health;
// }
//
// export enum Health {
//     Green = "Green",
//     Yellow = "Yellow",
//     Red = "Red"
// }
//
// export interface InternalPipelineConfiguration {
//     id: string;
//     name: string;
//     description: string;
//     filters: FilterConfiguration[];
//     isRunning: boolean;
// }
//
// export interface PipelineConfiguration {
//     id: string;
//     name: string;
//     description: string;
//     source: FilterConfiguration;
//     filter: FilterConfiguration[];
//     sink: FilterConfiguration;
// }
//
// // -----------------------Filter---------------------------
//
//
// export interface FilterDescriptor {
//     name: string;
//     displayName: string;
//     description: string;
//     previousConnection: FilterConnection;
//     nextConnection: FilterConnection;
//     parameters: ParameterDescriptor[];
//     category: string;
// }
//
// export interface FilterConnection {
//     isPermitted: boolean;
//     connectionType: string[];
// }
// export interface FilterInstanceState {
//     id: string;
//     health: Health;
//     throughPutTime: number;
//     totalThroughputTime: number;
//     status: FilterStatus;
// }
//
// export enum FilterStatus {
//     Unknown = "Unknown",
//     Paused = "Paused",
//     Running = "Running",
//     Drained = "Drained"
// }
//
// // ------------------Parameter Descriptors------------------
//
// export interface ParameterDescriptor {
//     name: string;
//     displayName: string;
//     jsonClass: string;
//     mandatory: boolean;
//     value?: any;
// }
//
// // ------------------Parameter for Configuration------------------
//
// export interface Parameter {
//     name: string;
//     value: any;
//     jsonClass: string;
// }
