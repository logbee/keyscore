import {Health} from "../common/Health";
import {ResourceStatus} from "./ResourceStatus";

export interface ResourceInstanceState {
    id: string;
    health: Health;
    throughPutTime: number;
    totalThroughputTime: number;
    status: ResourceStatus;
}
