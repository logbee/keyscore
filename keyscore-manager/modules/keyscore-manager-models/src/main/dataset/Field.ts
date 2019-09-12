import {Value} from "@keyscore-manager-models/src/main/dataset/Value";

export class Field {
    constructor(
        readonly name: string,
        readonly value: Value
    ) {
    }
}