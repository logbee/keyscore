import {TextRef} from "../../common/Localization";
import {Ref} from "../../common/Ref";

export interface ParameterDescriptor {
    name: string;
    displayName: string;
    jsonClass: string;
    mandatory: boolean;
    value?: any;
}

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

export interface NumberRange {
    step: number;
    start: number;
    end: number;
}

export interface ParameterInfo {
    displayName: TextRef;
    description: TextRef;
}

export interface ParameterDescriptorNew {
    ref: Ref;
    info: ParameterInfo;
    jsonClass: string;
}

export interface BooleanParameterDescriptor extends ParameterDescriptorNew {
    defaultValue: boolean;
    mandatory: boolean;

}

export interface TextParameterDescriptor extends ParameterDescriptorNew {
    defaultValue: string;
    validator: StringValidator;
    mandatory: boolean;

}

export interface ExpressionParameterDescriptor extends ParameterDescriptorNew {
    defaultValue: string;
    expressionType: ExpressionType;
    mandatory: boolean;

}

export interface NumberParameterDescriptor extends ParameterDescriptorNew {
    defaultValue: number;
    range: NumberRange;
    mandatory: boolean;

}

export interface FieldNameParameterDescriptor extends ParameterDescriptorNew {
    defaultValue: string;
    hint: FieldNameHint;
    validator: StringValidator;
    mandatory: boolean;

}

export interface FieldParameterDescriptor extends ParameterDescriptorNew {
    defaultName: string;
    hint: FieldNameHint;
    nameValidator: StringValidator;
    fieldValueType: FieldValueType;
    mandatory: boolean;

}

export interface TextListParameterDescriptor extends ParameterDescriptorNew {
    descriptor: TextParameterDescriptor;
    min: number;
    max: number;
}

export interface FieldNameListParameterDescriptor extends ParameterDescriptorNew {
    descriptor: FieldNameParameterDescriptor;
    min: number;
    max: number;
}

export interface FieldListParameterDescriptor extends ParameterDescriptorNew {
    descriptor: FieldParameterDescriptor;
    min: number;
    max: number;
}

export interface ChoiceParameterDescriptor extends ParameterDescriptorNew {
    min: number;
    max: number;
    choices: Choice[];
}

export interface Choice {
    name: string;
    displayName: TextRef;
    description: TextRef;
}

export interface ParameterGroupDescriptor extends ParameterDescriptorNew {
    condition: ParameterGroupCondition;
    parameters: ParameterDescriptorNew[];

}

export interface ParameterGroupCondition {
    jsonClass: string;
}

export interface BooleanParameterCondition {
    parameter: Ref;
    negate: boolean;
}




