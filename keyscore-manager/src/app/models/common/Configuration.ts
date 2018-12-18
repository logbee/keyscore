import {Parameter, ParameterSet} from "../parameters/Parameter";
import {Ref} from "./Ref";

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
    fieldName: string;
    directives: DirectiveConfiguration[];
}