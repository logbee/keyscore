import {Configuration} from "../common/Configuration";

export interface InternalPipelineConfiguration {
    id: string;
    name: string;
    description: string;
    filters: Configuration[];
    isRunning: boolean;
}