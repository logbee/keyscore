import {TextRef} from "../../common/Localization";
import {Ref} from "../../common/Ref";

export interface StringValidator {
    expression: string;
    expressionType: ExpressionType;
    description: TextRef;
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

export enum ParameterDescriptorJsonClass{
    TextParameterDescriptor = "io.logbee.keyscore.model.descriptor.TextParameterDescriptor",
    BooleanParameterDescriptor = "io.logbee.keyscore.model.descriptor.BooleanParameterDescriptor",
    ExpressionParameterDescriptor ="io.logbee.keyscore.model.descriptor.ExpressionParameterDescriptor",
    NumberParameterDescriptor ="io.logbee.keyscore.model.descriptor.NumberParameterDescriptor",
    DecimalParameterDescriptor ="io.logbee.keyscore.model.descriptor.DecimalParameterDescriptor",
    FieldNameParameterDescriptor ="io.logbee.keyscore.model.descriptor.FieldNameParameterDescriptor",
    FieldParameterDescriptor ="io.logbee.keyscore.model.descriptor.FieldParameterDescriptor",
    TextListParameterDescriptor ="io.logbee.keyscore.model.descriptor.TextListParameterDescriptor",
    FieldNameListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNameListParameterDescriptor",
    FieldListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldListParameterDescriptor",
    ChoiceParameterDescriptor = "io.logbee.keyscore.model.descriptor.ChoiceParameterDescriptor",
    ParameterGroupDescriptor = "io.logbee.keyscore.model.descriptor.ParameterGroupDescriptor",

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

export interface ResolvedParameterDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: string;
}

export interface BooleanParameterDescriptor extends ResolvedParameterDescriptor {
    defaultValue: boolean;
    mandatory: boolean;

}

export interface TextParameterDescriptor extends ResolvedParameterDescriptor {
    defaultValue: string;
    validator: StringValidator;
    mandatory: boolean;

}

export interface ExpressionParameterDescriptor extends ResolvedParameterDescriptor {
    defaultValue: string;
    expressionType: ExpressionType;
    mandatory: boolean;

}

export interface NumberParameterDescriptor extends ResolvedParameterDescriptor {
    defaultValue: number;
    range: NumberRange;
    mandatory: boolean;

}

export interface FieldNameParameterDescriptor extends ResolvedParameterDescriptor {
    defaultValue: string;
    hint: FieldNameHint;
    validator: StringValidator;
    mandatory: boolean;

}

export interface FieldParameterDescriptor extends ResolvedParameterDescriptor {
    defaultName: string;
    hint: FieldNameHint;
    nameValidator: StringValidator;
    fieldValueType: FieldValueType;
    mandatory: boolean;

}

export interface TextListParameterDescriptor extends ResolvedParameterDescriptor {
    descriptor: TextParameterDescriptor;
    min: number;
    max: number;
}

export interface FieldNameListParameterDescriptor extends ResolvedParameterDescriptor {
    descriptor: FieldNameParameterDescriptor;
    min: number;
    max: number;
}

export interface FieldListParameterDescriptor extends ResolvedParameterDescriptor {
    descriptor: FieldParameterDescriptor;
    min: number;
    max: number;
}

export interface ChoiceParameterDescriptor extends ResolvedParameterDescriptor {
    min: number;
    max: number;
    choices: Choice[];
}

export interface Choice {
    name: string;
    displayName: TextRef;
    description: TextRef;
}

export interface ParameterGroupDescriptor extends ResolvedParameterDescriptor {
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




