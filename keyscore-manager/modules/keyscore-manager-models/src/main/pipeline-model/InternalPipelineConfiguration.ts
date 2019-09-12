import {Configuration} from "@keyscore-manager-models";

export interface InternalPipelineConfiguration {
    id: string;
    name: string;
    description: string;
    filters: Configuration[];
    isRunning: boolean;
}