import {
    Parameter,
    ParameterDescriptor,
    Serializable
} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {StringValidator} from "@keyscore-manager-models/src/main/parameters/parameter-fields.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";

export const JSONCLASS_PASSWORD_PARAM = "io.logbee.keyscore.model.configuration.PasswordParameter";
export const JSONCLASS_PASSWORD_DESCR = "io.logbee.keyscore.model.descriptor.PasswordParameterDescriptor";

export class PasswordParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_PASSWORD_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly defaultValue: string,
        readonly validator: StringValidator,
        readonly minLength: number,
        readonly maxLength: number,
        readonly mandatory: boolean
    ) {
        super(ref, displayName, description);
    }
}

export class PasswordParameter implements Serializable{
    public readonly jsonClass = JSONCLASS_PASSWORD_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string
    ) {
    }
}
