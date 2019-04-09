import {Configuration} from "../../../../models/common/Configuration";

export interface PipelyPipelineConfiguration {
    id: string;
    name: string;
    description: string;
    blocks: Configuration[];
}