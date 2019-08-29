import {ParameterRef, Ref} from "../common";
import {PatternType} from "./parameter-fields.model";

export type Parameter =
    | DefaultParameter
    | FieldNamePatternParameter
    | ParameterGroup

export interface DefaultParameter {
    ref: ParameterRef;
    value: any;
    jsonClass: string;
}

export interface FieldNamePatternParameter extends DefaultParameter {
    ref: ParameterRef;
    value: string;
    patternType: PatternType;
    jsonClass: string;
}

export interface ParameterGroup {
    ref: ParameterRef;
    jsonClass: string;
    parameters: ParameterSet;
    value?:any;
}

export interface ParameterSet {
    jsonClass: string;
    parameters: Parameter[];
}

export enum ParameterJsonClass {
    BooleanParameter = "io.logbee.keyscore.model.configuration.BooleanParameter",
    TextParameter = "io.logbee.keyscore.model.configuration.TextParameter",
    ExpressionParameter = "io.logbee.keyscore.model.configuration.ExpressionParameter",
    NumberParameter = "io.logbee.keyscore.model.configuration.NumberParameter",
    DecimalParameter = "io.logbee.keyscore.model.configuration.DecimalParameter",
    FieldNameParameter = "io.logbee.keyscore.model.configuration.FieldNameParameter",
    FieldNamePatternParameter = "io.logbee.keyscore.model.configuration.FieldNamePatternParameter",
    FieldParameter = "io.logbee.keyscore.model.configuration.FieldParameter",
    TextListParameter = "io.logbee.keyscore.model.configuration.TextListParameter",
    FieldNameListParameter = "io.logbee.keyscore.model.configuration.FieldNameListParameter",
    FieldListParameter = "io.logbee.keyscore.model.configuration.FieldListParameter",
    ChoiceParameter = "io.logbee.keyscore.model.configuration.ChoiceParameter",
    FieldDirectiveSequenceParameter = "io.logbee.keyscore.model.configuration.FieldDirectiveSequenceParameter",
    ParameterSet = "io.logbee.keyscore.model.configuration.ParameterSet",
    ParameterGroup = "io.logbee.keyscore.model.configuration.ParameterGroup"
}

