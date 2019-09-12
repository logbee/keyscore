import {Ref} from "@keyscore-manager-models/src/main/common/Ref";
import {Parameter} from "../parameters/parameter.model";

export interface Configuration {
    ref: Ref;
    parent: Ref;
    parameterSet: ParameterSet;
}

export interface DirectiveConfiguration {
    ref: Ref;
    instance: Ref;
    parameters: ParameterSet;
}

export interface FieldDirectiveSequenceConfiguration {
    id: string;
    parameters:ParameterSet;
    directives: DirectiveConfiguration[];
}

export const JSONCLASS_PARAMETERSET = "io.logbee.keyscore.model.configuration.ParameterSet";

export interface ParameterSet{
    parameters:Parameter[];
}