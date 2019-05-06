import {Health} from "keyscore-manager-models";

export interface PipelineTableModel {
    uuid: string,
    health: Health,
    name: string,
    description: string
}