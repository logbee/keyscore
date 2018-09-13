import {BlockConfiguration} from "./block-configuration.model";

export interface PipelyPipelineConfiguration {
    id: string;
    name: string;
    description: string;
    blocks: BlockConfiguration[];
}