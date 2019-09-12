import {Health} from "@keyscore-manager-models/src/main/common/Health";

export interface PipelineInstance {
    id: string;
    name: string;
    description: string;
    configurationId: string;
    health: Health;
}
