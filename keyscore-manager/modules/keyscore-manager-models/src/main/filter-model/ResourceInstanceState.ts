import {Health} from "@keyscore-manager-models/src/main/common/Health";
import {ResourceStatus} from "@keyscore-manager-models/src/main/filter-model/ResourceStatus";

export interface ResourceInstanceState {
    id: string;
    health: Health;
    throughPutTime: number;
    totalThroughputTime: number;
    status: ResourceStatus;
}
