import {ResourceInstanceState} from "@keyscore-manager-models";

export class StateObject {

    constructor(
        public resourceId: string,
        public resourceInstance: ResourceInstanceState) {
    }
}
