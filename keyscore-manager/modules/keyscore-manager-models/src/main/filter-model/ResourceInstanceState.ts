import {Health,ResourceStatus} from "@keyscore-manager-models";

export interface ResourceInstanceState {
    id: string;
    health: Health;
    throughPutTime: number;
    totalThroughputTime: number;
    status: ResourceStatus;
}
