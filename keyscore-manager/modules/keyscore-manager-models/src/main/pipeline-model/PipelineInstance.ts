import {Health} from "../common/Health";

export interface PipelineInstance {
    id: string;
    name: string;
    description: string;
    configurationId: string;
    health: Health;
}
