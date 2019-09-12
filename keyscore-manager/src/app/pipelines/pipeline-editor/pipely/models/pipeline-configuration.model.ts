import {Configuration} from "@/../modules/keyscore-manager-models/src/main/common/Configuration";

export interface PipelyPipelineConfiguration {
    id: string;
    name: string;
    description: string;
    blocks: Configuration[];
}