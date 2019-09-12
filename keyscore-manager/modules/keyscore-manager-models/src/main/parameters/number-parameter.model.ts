import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {ParameterRef} from "@keyscore-manager-models";
import {NumberRange} from "@keyscore-manager-models/src/main/parameters/parameter-fields.model";

export const JSONCLASS_NUMBER_PARAM = "io.logbee.keyscore.model.configuration.NumberParameter";
export const JSONCLASS_NUMBER_DESCR = "io.logbee.keyscore.model.descriptor.NumberParameterDescriptor";

export class NumberParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_NUMBER_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly defaultValue: number,
        readonly range: NumberRange,
        readonly mandatory: boolean
    ) {
        super(ref, displayName, description);
    }
}

export class NumberParameter extends Parameter {
    public readonly jsonClass = JSONCLASS_NUMBER_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: number
    ) {
        super(ref, value);
    }
}