import {FilterConfiguration} from "../filter-model/FilterConfiguration";

export interface InternalPipelineConfiguration {
    id: string;
    name: string;
    description: string;
    filters: FilterConfiguration[];
    isRunning: boolean;
}