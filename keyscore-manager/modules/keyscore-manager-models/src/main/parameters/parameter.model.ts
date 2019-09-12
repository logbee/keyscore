import {ParameterRef} from "@keyscore-manager-models/src/main/common";

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

export enum ParameterDescriptorJsonClass{
    BooleanParameterDescriptor = "io.logbee.keyscore.model.descriptor.BooleanParameterDescriptor",
    NumberParameterDescriptor = "io.logbee.keyscore.model.descriptor.NumberParameterDescriptor",
    TextParameterDescriptor = "io.logbee.keyscore.model.descriptor.TextParameterDescriptor",
    ChoiceParameterDescriptor = "io.logbee.keyscore.model.descriptor.ChoiceParameterDescriptor",
    DecimalParameterDescriptor = "io.logbee.keyscore.model.descriptor.DecimalParameterDescriptor",
    ExpressionParameterDescriptor = "io.logbee.keyscore.model.descriptor.ExpressionParameterDescriptor",
    FieldNameParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNameParameterDescriptor",
    FieldNamePatternParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNamePatternParameterDescriptor",
    FieldParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldParameterDescriptor",
    FieldListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldListParameterDescriptor",
    FieldNameListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNameListParameterDescriptor",
    TextListParameterDescriptor = "io.logbee.keyscore.model.descriptor.TextListParameterDescriptor"
}