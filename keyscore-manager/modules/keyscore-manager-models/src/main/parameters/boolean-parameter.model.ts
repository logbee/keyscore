import {ParameterRef} from "@keyscore-manager-models";
import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";

export const JSONCLASS_BOOLEAN_PARAM = "io.logbee.keyscore.model.configuration.BooleanParameter";
export const JSONCLASS_BOOLEAN_DESCR = "io.logbee.keyscore.model.descriptor.BooleanParameterDescriptor";

export class BooleanParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_BOOLEAN_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly defaultValue: boolean,
        readonly mandatory: boolean
    ) {
        super(ref, displayName, description);
    }
}

export class BooleanParameter extends Parameter {
    public readonly jsonClass = JSONCLASS_BOOLEAN_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: boolean
    ) {
        super(ref, value);
    }
}