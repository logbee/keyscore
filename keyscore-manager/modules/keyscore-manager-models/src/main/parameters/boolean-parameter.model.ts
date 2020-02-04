import {
    Parameter,
    ParameterDescriptor,
    Serializable
} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";

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

export class BooleanParameter implements Serializable {
    public readonly jsonClass = JSONCLASS_BOOLEAN_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: boolean
    ) {
    }
}
