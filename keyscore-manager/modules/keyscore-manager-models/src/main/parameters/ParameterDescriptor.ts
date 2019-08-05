import {ParameterRef, Ref, TextRef} from "../common";

export interface StringValidator {
    expression: string;
    expressionType: ExpressionType;
    description: TextRef;
}

export interface ResolvedStringValidator {
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
    AnyField = "AnyField",
    PresentField = "PresentField",
    AbsentField = "AbsentField"
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

export interface Range{
    step:number;
    start:number;
    end:number;
}
export interface NumberRange extends Range {
    step: number;
    start: number;
    end: number;
}

export interface DecimalRange extends Range{
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
    ref: ParameterRef;
    info: ParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    validator?: StringValidator;
    nameValidator?: StringValidator;
    mandatory?: boolean;
    defaultValue?: any;
    expressionType?: ExpressionType;
    range?: NumberRange;
    decimals?: number;
    hint?: FieldNameHint;
    min?: number;
    max?: number;
    choices?: Choice[];
    descriptor?: ParameterDescriptor;
    fieldTypes?: FieldValueType;
    parameters?: ParameterDescriptor[];
    directives?: FieldDirectiveDescriptor[];
    minSequences?: number;
    maxSequences?: number;
    fieldValueType?: FieldValueType;
    condition?:BooleanParameterCondition;
    supports?:PatternType[];


}

export enum ParameterDescriptorJsonClass {
    TextParameterDescriptor = "io.logbee.keyscore.model.descriptor.TextParameterDescriptor",
    BooleanParameterDescriptor = "io.logbee.keyscore.model.descriptor.BooleanParameterDescriptor",
    ExpressionParameterDescriptor = "io.logbee.keyscore.model.descriptor.ExpressionParameterDescriptor",
    NumberParameterDescriptor = "io.logbee.keyscore.model.descriptor.NumberParameterDescriptor",
    DecimalParameterDescriptor = "io.logbee.keyscore.model.descriptor.DecimalParameterDescriptor",
    FieldNameParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNameParameterDescriptor",
    FieldNamePatternParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNamePatternParameterDescriptor",
    FieldParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldParameterDescriptor",
    TextListParameterDescriptor = "io.logbee.keyscore.model.descriptor.TextListParameterDescriptor",
    FieldNameListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNameListParameterDescriptor",
    FieldListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldListParameterDescriptor",
    ChoiceParameterDescriptor = "io.logbee.keyscore.model.descriptor.ChoiceParameterDescriptor",
    ParameterGroupDescriptor = "io.logbee.keyscore.model.descriptor.ParameterGroupDescriptor",
    BooleanParameterCondition = "io.logbee.keyscore.model.descriptor.BooleanParameterCondition",
    FieldDirectiveSequenceParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldDirectiveSequenceParameterDescriptor"
}

export enum DirectiveDescriptorJsonClass {
    FieldDirectiveDescriptor = "io.logbee.keyscore.model.descriptor.FieldDirectiveDescriptor"
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
    | FieldNamePatternParameterDescriptor
    | FieldListParameterDescriptor
    | ChoiceParameterDescriptor
    | ParameterGroupDescriptor
    | FieldDirectiveSequenceParameterDescriptor;

export type SingleResolvedParameterDescriptor =
    | BooleanParameterDescriptor
    | TextParameterDescriptor
    | ExpressionParameterDescriptor
    | NumberParameterDescriptor
    | DecimalParameterDescriptor
    | FieldNameParameterDescriptor
    | FieldNamePatternParameterDescriptor
    | FieldParameterDescriptor;

export type ListResolvedParameterDescriptor =
    | TextListParameterDescriptor
    | FieldNameListParameterDescriptor
    | FieldListParameterDescriptor
    | FieldDirectiveSequenceParameterDescriptor
    | ChoiceParameterDescriptor;

export interface BooleanParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    defaultValue: boolean;
    mandatory: boolean;

}

export interface TextParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    defaultValue: string;
    validator: ResolvedStringValidator;
    mandatory: boolean;

}

export interface ExpressionParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    defaultValue: string;
    expressionType: ExpressionType;
    mandatory: boolean;

}

export interface NumberParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    defaultValue: number;
    range: NumberRange;
    mandatory: boolean;
}

export interface DecimalParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    defaultValue: number;
    range: NumberRange;
    decimals: number;
    mandatory: boolean;

}

export interface FieldNameParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    defaultValue: string;
    hint: FieldNameHint;
    validator: ResolvedStringValidator;
    mandatory: boolean;

}

export interface FieldNamePatternParameterDescriptor {
    ref: ParameterRef;
    info: ParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    defaultValue: string;
    hint: FieldNameHint;
    supports: PatternType[];
    mandatory: boolean;
}

export enum PatternType {
    None = 0, RegEx = 1, Glob = 2
}

export function PatternTypeToString(type: PatternType) {
    return type === PatternType.None ? "None" : type === PatternType.Glob ? "Glob Pattern" : "Regular Expression";
}

export interface FieldParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    defaultName: string;
    hint: FieldNameHint;
    nameValidator: ResolvedStringValidator;
    fieldValueType: FieldValueType;
    mandatory: boolean;

}

export interface TextListParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    descriptor: TextParameterDescriptor;
    min: number;
    max: number;
}

export interface FieldNameListParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    descriptor: FieldNameParameterDescriptor;
    min: number;
    max: number;
}

export interface FieldListParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    descriptor: FieldParameterDescriptor;
    min: number;
    max: number;
}

export interface ChoiceParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    min: number;
    max: number;
    choices: ResolvedChoice[];
}


export interface Choice {
    name: string;
    displayName: TextRef;
    description: TextRef;
}

export interface ResolvedChoice {
    name: string;
    displayName: string;
    description: string;
}

export interface ParameterGroupDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    condition: ParameterGroupCondition;
    parameters: ResolvedParameterDescriptor[];

}

export interface FieldDirectiveSequenceParameterDescriptor {
    ref: ParameterRef;
    info: ResolvedParameterInfo;
    jsonClass: ParameterDescriptorJsonClass;
    fieldTypes: FieldValueType;
    parameters: ResolvedParameterDescriptor[];
    directives: ResolvedFieldDirectiveDescriptor[];
    minSequences: number;
    maxSequences: number;
}

export interface FieldDirectiveDescriptor {
    ref: Ref;
    info: ParameterInfo;
    jsonClass: DirectiveDescriptorJsonClass;
    parameters: ParameterDescriptor[];
    minSequences: number;
    maxSequences: number;
}


export interface ResolvedFieldDirectiveDescriptor {
    ref: Ref;
    info: ResolvedParameterInfo;
    jsonClass: DirectiveDescriptorJsonClass;
    parameters: ResolvedParameterDescriptor[];
    minSequences: number;
    maxSequences: number;
}

export type ParameterGroupCondition =
    |BooleanParameterCondition

export interface BooleanParameterCondition{
    jsonClass:string;
    parameter: ParameterRef;
    negate: boolean;
}






