import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";

export interface PipelineConfiguration {
    id: string;
    name: string;
    description: string;
    source: Configuration;
    filter: Configuration[];
    sink: Configuration;
}