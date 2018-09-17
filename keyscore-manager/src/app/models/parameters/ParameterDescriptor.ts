import {TextRef} from "../common/Localization";
import {Ref} from "../common/Ref";

export interface StringValidator {
    expression: string;
    expressionType: ExpressionType;
    description: TextRef;
}

export interface ResolvedStringValidator{
    expression: string;
    expressionType: ExpressionType;
    description: string;
}

export enum ExpressionType {
    RegEx = 0,
    Grok = 2,
    Glob = 3,
    JSONPath = 4
}

export enum FieldNameHint {
    AnyField = 0,
    PresentField = 1,
    AbsentField = 2
}

export enum FieldValueType {
    Unknown = 0,
    Boolean = 1,
    Number = 2,
    Decimal = 3,
    Text = 4,
    Timestamp = 5,
    Duration = 6
}


export interface NumberRange {
    step: number;
    start: number;
    end: number;
}

export interface ParameterInfo {
    displayName: TextRef;
    description: TextRef;
}

export interface ResolvedParameterInfo {
    displayName: string;
    description: string;
}

export interface ParameterDescriptor {
    ref: Ref;
    info: ParameterInfo;
    jsonClass: string;
}

export const ParameterDescriptorPackagePrefix = "io.logbee.keyscore.model.descriptor";

export enum ParameterDescriptorJsonClass {
    TextParameterDescriptor = "io.logbee.keyscore.model.descriptor.TextParameterDescriptor",
    BooleanParameterDescriptor = "io.logbee.keyscore.model.descriptor.BooleanParameterDescriptor",
    ExpressionParameterDescriptor = "io.logbee.keyscore.model.descriptor.ExpressionParameterDescriptor",
    NumberParameterDescriptor = "io.logbee.keyscore.model.descriptor.NumberParameterDescriptor",
    DecimalParameterDescriptor = "io.logbee.keyscore.model.descriptor.DecimalParameterDescriptor",
    FieldNameParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNameParameterDescriptor",
    FieldParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldParameterDescriptor",
    TextListParameterDescriptor = "io.logbee.keyscore.model.descriptor.TextListParameterDescriptor",
    FieldNameListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNameListParameterDescriptor",
    FieldListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldListParameterDescriptor",
    ChoiceParameterDescriptor = "io.logbee.keyscore.model.descriptor.ChoiceParameterDescriptor",
    ParameterGroupDescriptor = "io.logbee.keyscore.model.descriptor.ParameterGroupDescriptor"
}

export type ResolvedParameterDescriptor =
    | BooleanParameterDescriptor
    | TextParameterDescriptor
    | ExpressionParameterDescriptor
    | NumberParameterDescriptor
    | DecimalParameterDescriptor
    | FieldNameParameterDescriptor
    | FieldParameterDescriptor
    | TextListParameterDescriptor
    | FieldNameListParameterDescriptor
    | FieldListParameterDescriptor
    | ChoiceParameterDescriptor
    | ParameterGroupDescriptor;

export interface BooleanParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    defaultValue: boolean;
    mandatory: boolean;

}

export interface TextParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    defaultValue: string;
    validator: ResolvedStringValidator;
    mandatory: boolean;

}

export interface ExpressionParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    defaultValue: string;
    expressionType: ExpressionType;
    mandatory: boolean;

}

export interface NumberParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    defaultValue: number;
    range: NumberRange;
    mandatory: boolean;
}

export interface DecimalParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    defaultValue: number;
    range: NumberRange;
    decimals: number;
    mandatory: boolean;

}

export interface FieldNameParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    defaultValue: string;
    hint: FieldNameHint;
    validator: ResolvedStringValidator;
    mandatory: boolean;

}

export interface FieldParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    defaultName: string;
    hint: FieldNameHint;
    nameValidator: ResolvedStringValidator;
    fieldValueType: FieldValueType;
    mandatory: boolean;

}

export interface TextListParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    descriptor: TextParameterDescriptor;
    min: number;
    max: number;
}

export interface FieldNameListParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    descriptor: FieldNameParameterDescriptor;
    min: number;
    max: number;
}

export interface FieldListParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    descriptor: FieldParameterDescriptor;
    min: number;
    max: number;
}

export interface ChoiceParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    min: number;
    max: number;
    choices: Choice[];
}

export interface Choice {
    name: string;
    displayName: TextRef;
    description: TextRef;
}

export interface ParameterGroupDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
    condition: ParameterGroupCondition;
    parameters: ParameterDescriptor[];

}

export interface ParameterGroupCondition {
    jsonClass: string;
}

export interface BooleanParameterCondition {
    parameter: Ref;
    negate: boolean;
}




