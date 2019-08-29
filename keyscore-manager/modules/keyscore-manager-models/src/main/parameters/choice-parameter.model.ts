import {Parameter, ParameterDescriptor} from "./parameter.model";
import {Choice} from "./parameter-fields.model";
import {ParameterRef} from "@keyscore-manager-models";


export const JSONCLASS_CHOICE_PARAM = "io.logbee.keyscore.model.configuration.ChoiceParameter";
export const JSONCLASS_CHOICE_DESCR = "io.logbee.keyscore.model.descriptor.ChoiceParameterDescriptor";

export class ChoiceParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_CHOICE_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly min: number,
        readonly max: number,
        readonly choices: Choice[]
    ) {
        super(ref, displayName, description)
    }
}

export class ChoiceParameter extends Parameter {
    public readonly jsonClass = JSONCLASS_CHOICE_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string
    ) {
        super(ref, value);
    }
}
