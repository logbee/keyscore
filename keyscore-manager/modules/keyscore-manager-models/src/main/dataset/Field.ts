import {Value} from "@keyscore-manager-models";

export class Field {
    constructor(
        readonly name: string,
        readonly value: Value
    ) {
    }
}