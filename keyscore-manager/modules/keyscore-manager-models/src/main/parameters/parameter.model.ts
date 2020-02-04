import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";
import {
    FieldDirectiveSequenceParameter,
    JSONCLASS_DIRECTIVE_DESCR,
    JSONCLASS_DIRECTIVE_SEQ_DESCR
} from "@keyscore-manager-models/src/main/parameters/directive.model";
import {
    JSONCLASS_GROUP_DESCR,
    ParameterGroup
} from "@keyscore-manager-models/src/main/parameters/group-parameter.model";
import {
    JSONCLASS_TEXTLIST_DESCR,
    TextListParameter
} from "@keyscore-manager-models/src/main/parameters/parameter-lists/text-list-parameter.model";
import {
    FieldNameListParameter,
    JSONCLASS_FIELDNAMELIST_DESCR
} from "@keyscore-manager-models/src/main/parameters/parameter-lists/field-name-list-parameter.model";
import {
    FieldListParameter,
    JSONCLASS_FIELDLIST_DESCR
} from "@keyscore-manager-models/src/main/parameters/parameter-lists/field-list-parameter.model";
import {
    FieldParameter,
    JSONCLASS_FIELD_DESCR
} from "@keyscore-manager-models/src/main/parameters/field-parameter.model";
import {
    FieldNamePatternParameter,
    JSONCLASS_FIELDNAMEPATTERN_DESCR
} from "@keyscore-manager-models/src/main/parameters/field-name-pattern-parameter.model";
import {
    FieldNameParameter,
    JSONCLASS_FIELDNAME_DESCR
} from "@keyscore-manager-models/src/main/parameters/field-name-parameter.model";
import {
    ExpressionParameter,
    JSONCLASS_EXPRESSION_DESCR
} from "@keyscore-manager-models/src/main/parameters/expression-parameter.model";
import {
    DecimalParameter,
    JSONCLASS_DECIMAL_DESCR
} from "@keyscore-manager-models/src/main/parameters/decimal-parameter.model";
import {
    ChoiceParameter,
    JSONCLASS_CHOICE_DESCR
} from "@keyscore-manager-models/src/main/parameters/choice-parameter.model";
import {
    JSONCLASS_PASSWORD_DESCR,
    PasswordParameter
} from "@keyscore-manager-models/src/main/parameters/password-parameter.model";
import {JSONCLASS_TEXT_DESCR, TextParameter} from "@keyscore-manager-models/src/main/parameters/text-parameter.model";
import {
    JSONCLASS_NUMBER_DESCR,
    NumberParameter
} from "@keyscore-manager-models/src/main/parameters/number-parameter.model";
import {
    BooleanParameter,
    JSONCLASS_BOOLEAN_DESCR
} from "@keyscore-manager-models/src/main/parameters/boolean-parameter.model";

export abstract class ParameterDescriptor implements Serializable{
    public readonly jsonClass: string;

    protected constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string
    ) {
    }
}

export interface Serializable {
    readonly jsonClass: string;
}

export type Parameter = TextParameter
    | NumberParameter
    | FieldNameParameter
    | BooleanParameter
    | PasswordParameter
    | ChoiceParameter
    | DecimalParameter
    | ExpressionParameter
    | FieldNamePatternParameter
    | FieldParameter
    | ParameterGroup
    | FieldDirectiveSequenceParameter
    | ListParameter;

export type ListParameter = FieldListParameter
    | TextListParameter
    | FieldNameListParameter

export interface ParameterMap {
    parameters: { [ref: string]: [Parameter, ParameterDescriptor] }
}

export enum ParameterDescriptorJsonClass {
    BooleanParameterDescriptor = "io.logbee.keyscore.model.descriptor.BooleanParameterDescriptor",
    NumberParameterDescriptor = "io.logbee.keyscore.model.descriptor.NumberParameterDescriptor",
    TextParameterDescriptor = "io.logbee.keyscore.model.descriptor.TextParameterDescriptor",
    PasswordParameterDescriptor = "io.logbee.keyscore.model.descriptor.PasswordParameterDescriptor",
    ChoiceParameterDescriptor = "io.logbee.keyscore.model.descriptor.ChoiceParameterDescriptor",
    DecimalParameterDescriptor = "io.logbee.keyscore.model.descriptor.DecimalParameterDescriptor",
    ExpressionParameterDescriptor = "io.logbee.keyscore.model.descriptor.ExpressionParameterDescriptor",
    FieldNameParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNameParameterDescriptor",
    FieldNamePatternParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNamePatternParameterDescriptor",
    FieldParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldParameterDescriptor",
    FieldListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldListParameterDescriptor",
    FieldNameListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNameListParameterDescriptor",
    TextListParameterDescriptor = "io.logbee.keyscore.model.descriptor.TextListParameterDescriptor",
    ParameterGroupDescriptor = "io.logbee.keyscore.model.descriptor.ParameterGroupDescriptor",
    FieldDirectiveSequenceParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldDirectiveSequenceParameterDescriptor"
}
