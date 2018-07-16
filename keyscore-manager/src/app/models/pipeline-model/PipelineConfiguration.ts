import {FilterConfiguration} from "../filter-model/FilterConfiguration";

export interface PipelineConfiguration {
    id: string;
    name: string;
    description: string;
    source: FilterConfiguration;
    filter: FilterConfiguration[];
    sink: FilterConfiguration;
}