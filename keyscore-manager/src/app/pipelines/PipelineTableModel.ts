import {Health} from "@/../modules/keyscore-manager-models/src/main/common/Health";

export interface PipelineTableModel {
    uuid: string,
    health: Health,
    name: string,
    description: string
}