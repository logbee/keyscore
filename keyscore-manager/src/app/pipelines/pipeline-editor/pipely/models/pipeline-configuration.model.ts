import {Configuration} from "@keyscore-manager-models";

export interface PipelyPipelineConfiguration {
    id: string;
    name: string;
    description: string;
    blocks: Configuration[];
}