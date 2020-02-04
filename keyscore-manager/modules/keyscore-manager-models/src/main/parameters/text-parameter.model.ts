import {
    Parameter,
    ParameterDescriptor,
    Serializable
} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {StringValidator} from "@keyscore-manager-models/src/main/parameters/parameter-fields.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";

export const JSONCLASS_TEXT_PARAM = "io.logbee.keyscore.model.configuration.TextParameter";
export const JSONCLASS_TEXT_DESCR = "io.logbee.keyscore.model.descriptor.TextParameterDescriptor";

export class TextParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_TEXT_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly defaultValue: string,
        readonly validator: StringValidator,
        readonly mandatory: boolean
    ) {
        super(ref, displayName, description);
    }
}

export class TextParameter implements Serializable {
    public readonly jsonClass = JSONCLASS_TEXT_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string
    ) {
    }
}
