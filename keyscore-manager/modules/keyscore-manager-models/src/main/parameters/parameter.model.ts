import {ParameterRef} from "@keyscore-manager-models";

export abstract class ParameterDescriptor {
    public readonly jsonClass: string;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string
    ) {
    }
}

export abstract class Parameter {
    public readonly jsonClass: string;

    constructor(
        readonly ref: ParameterRef,
        readonly value: any
    ) {
    }
}

export interface ParameterMap {
    parameters: { [ref: string]: [Parameter, ParameterDescriptor] }
}