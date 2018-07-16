import {Health} from "../common/Health";
import {FilterStatus} from "./FilterStatus";

export interface FilterInstanceState {
    id: string;
    health: Health;
    throughPutTime: number;
    totalThroughputTime: number;
    status: FilterStatus;
}
