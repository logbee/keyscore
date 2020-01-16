import {TextRef} from "@keyscore-manager-models/src/main/common/Localization";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";
import {ParameterGroupCondition} from "@keyscore-manager-models/src/main/parameters/group-parameter.model";
import {Icon} from "@keyscore-manager-models/src/main/descriptors/Icon";
import {DirectiveRef} from "@keyscore-manager-models/src/main/parameters/directive.model";

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
    RegEx = "RegEx",
    Grok = "Grok",
    Glob = "Glob",
    JSONPath = "JSONPath"
}

export enum FieldNameHint {
    AnyField = "AnyField",
    PresentField = "PresentField",
    AbsentField = "AbsentField"
}

export enum FieldValueType {
    Unknown = "Unknown",
    Boolean = "Boolean",
    Number = "Number",
    Decimal = "Decimal",
    Text = "Text",
    Timestamp = "Timestamp",
    Duration = "Duration"
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
    minSequences?: number;
    maxSequences?: number;
    fieldValueType?: FieldValueType;
    condition?: ParameterGroupCondition;
    supports?: PatternType[];
    minLength?: number,
    maxLength?: number,
    icon?: Icon,
    directives?: FieldDirectiveDescriptorWithLocales[]
}

export interface FieldDirectiveDescriptorWithLocales {
    ref: DirectiveRef,
    info: ParameterInfoWithLocales,
    jsonClass: string,
    parameters?: ParameterDescriptorWithLocales[],
    icon?: Icon
}


export enum PatternType {
    ExactMatch = "ExactMatch", RegEx = "RegEx", Glob = "Glob"
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







