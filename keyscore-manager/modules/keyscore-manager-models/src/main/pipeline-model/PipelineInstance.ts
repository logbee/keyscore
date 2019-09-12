import {Health} from "@keyscore-manager-models";

export interface PipelineInstance {
    id: string;
    name: string;
    description: string;
    configurationId: string;
    health: Health;
}
