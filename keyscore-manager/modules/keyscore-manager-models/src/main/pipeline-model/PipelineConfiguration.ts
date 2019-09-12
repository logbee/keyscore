import {Configuration} from "@keyscore-manager-models";

export interface PipelineConfiguration {
    id: string;
    name: string;
    description: string;
    source: Configuration;
    filter: Configuration[];
    sink: Configuration;
}