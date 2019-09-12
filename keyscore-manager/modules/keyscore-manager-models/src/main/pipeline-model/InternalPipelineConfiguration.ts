import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";

export interface InternalPipelineConfiguration {
    id: string;
    name: string;
    description: string;
    filters: Configuration[];
    isRunning: boolean;
}