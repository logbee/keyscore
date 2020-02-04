import {
    Parameter,
    ParameterDescriptor,
    Serializable
} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {
    FieldNameHint,
    FieldValueType,
    StringValidator
} from "@keyscore-manager-models/src/main/parameters/parameter-fields.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";
import {Field} from "@keyscore-manager-models/src/main/dataset/Field";

export const JSONCLASS_FIELD_PARAM = "io.logbee.keyscore.model.configuration.FieldParameter";
export const JSONCLASS_FIELD_DESCR = "io.logbee.keyscore.model.descriptor.FieldParameterDescriptor";

export class FieldParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_FIELD_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly defaultName: string,
        readonly hint: FieldNameHint,
        readonly nameValidator: StringValidator,
        readonly fieldValueType: FieldValueType,
        readonly mandatory: boolean
    ) {
        super(ref, displayName, description);
    }
}

export class FieldParameter implements Serializable {
    public readonly jsonClass = JSONCLASS_FIELD_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: Field
    ) {
    }
}
