import {ParameterRef, Ref} from "../common/Ref";

export interface Parameter {
    ref: ParameterRef;
    value: any;
    jsonClass: string;
}

export const ParameterPackagePrefix = "io.logbee.keyscore.model.configuration";

export enum ParameterJsonClass {
    BooleanParameter = "io.logbee.keyscore.model.configuration.BooleanParameter",
    TextParameter = "io.logbee.keyscore.model.configuration.TextParameter",
    ExpressionParameter = "io.logbee.keyscore.model.configuration.ExpressionParameter",
    NumberParameter = "io.logbee.keyscore.model.configuration.NumberParameter",
    DecimalParameter = "io.logbee.keyscore.model.configuration.DecimalParameter",
    FieldNameParameter = "io.logbee.keyscore.model.configuration.FieldNameParameter",
    FieldParameter = "io.logbee.keyscore.model.configuration.FieldParameter",
    TextListParameter = "io.logbee.keyscore.model.configuration.TextListParameter",
    FieldNameListParameter = "io.logbee.keyscore.model.configuration.FieldNameListParameter",
    FieldListParameter = "io.logbee.keyscore.model.configuration.FieldListParameter",
    ChoiceParameter = "io.logbee.keyscore.model.configuration.ChoiceParameter",
    FieldDirectiveSequenceParameter = "io.logbee.keyscore.model.configuration.FieldDirectiveSequenceParameter",
    ParameterSet = "io.logbee.keyscore.model.configuration.ParameterSet"
}

export interface ParameterSet {
    jsonClass:string;
    parameters: Parameter[];
}