import {Parameter, ParameterDescriptor} from "../parameter.model";
import {ParameterRef, FieldNameHint, ResolvedStringValidator} from "keyscore-manager-models";

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
        readonly validator: ResolvedStringValidator,
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