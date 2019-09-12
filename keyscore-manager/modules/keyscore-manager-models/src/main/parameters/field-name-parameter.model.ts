import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {FieldNameHint, StringValidator} from "@keyscore-manager-models/src/main/parameters/parameter-fields.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";

export const JSONCLASS_FIELDNAME_PARAM = "io.logbee.keyscore.model.configuration.FieldNameParameter";
export const JSONCLASS_FIELDNAME_DESCR = "io.logbee.keyscore.model.descriptor.FieldNameParameterDescriptor";

export class FieldNameParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_FIELDNAME_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly defaultValue: string,
        readonly hint: FieldNameHint,
        readonly validator: StringValidator,
        readonly mandatory: boolean
    ) {
        super(ref, displayName, description);
    }
}

export class FieldNameParameter extends Parameter {
    public readonly jsonClass = JSONCLASS_FIELDNAME_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string
    ) {
        super(ref, value);
    }
}