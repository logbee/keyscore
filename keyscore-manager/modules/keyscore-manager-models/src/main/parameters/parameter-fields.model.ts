import {TextRef} from "@keyscore-manager-models/src/main/common/Localization";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";

export interface StringValidatorWithLocales {
    expression: string;
    expressionType: ExpressionType;
    description: TextRef;
}

export interface StringValidator {
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

export interface NumberRange {
    step: number;
    start: number;
    end: number;
}

export interface ParameterInfoWithLocales {
    displayName: TextRef;
    description: TextRef;
}

export interface ParameterInfo {
    displayName: string;
    description: string;
}

export interface ParameterDescriptorWithLocales {
    ref: ParameterRef;
    info: ParameterInfoWithLocales;
    jsonClass: string;
    validator?: StringValidatorWithLocales;
    nameValidator?: StringValidatorWithLocales;
    mandatory?: boolean;
    defaultValue?: any;
    expressionType?: ExpressionType;
    range?: NumberRange;
    decimals?: number;
    hint?: FieldNameHint;
    min?: number;
    max?: number;
    choices?: ChoiceWithLocales[];
    descriptor?: ParameterDescriptorWithLocales;
    fieldTypes?: FieldValueType;
    parameters?: ParameterDescriptorWithLocales[];
    directives?: any;
    minSequences?: number;
    maxSequences?: number;
    fieldValueType?: FieldValueType;
    condition?: BooleanParameterCondition;
    supports?: PatternType[];
}


export enum PatternType {
    None = 0, RegEx = 1, Glob = 2
}


export interface ChoiceWithLocales {
    name: string;
    displayName: TextRef;
    description: TextRef;
}

export interface Choice {
    name: string;
    displayName: string;
    description: string;
}

export type ParameterGroupCondition =
    |BooleanParameterCondition

export interface BooleanParameterCondition {
    jsonClass: string;
    parameter: ParameterRef;
    negate: boolean;
}






