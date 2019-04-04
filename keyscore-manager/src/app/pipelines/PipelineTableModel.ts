import {Health} from "../models/common/Health";

export interface PipelineTableModel {
    uuid: string,
    health: Health,
    name: string,
    description: string
}