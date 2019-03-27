import {Configuration} from "../common/Configuration";

export interface PipelineConfiguration {
    id: string;
    name: string;
    description: string;
    source: Configuration;
    filter: Configuration[];
    sink: Configuration;
}