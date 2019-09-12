import {ResourceInstanceState} from "@keyscore-manager-models/src/main/filter-model/ResourceInstanceState";

export class StateObject {

    constructor(
        public resourceId: string,
        public resourceInstance: ResourceInstanceState) {
    }
}
