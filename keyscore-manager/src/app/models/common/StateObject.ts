import {ResourceInstanceState} from "../filter-model/ResourceInstanceState";

export class StateObject {

     resourceId: string;
     resourceInstance: ResourceInstanceState;

    constructor(resourceId: string, resourceInstance: ResourceInstanceState) {
        this.resourceId = resourceId;
        this.resourceInstance = resourceInstance;
    }
}
