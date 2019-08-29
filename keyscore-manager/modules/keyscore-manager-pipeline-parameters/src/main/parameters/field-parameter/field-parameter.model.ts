import {Parameter, ParameterDescriptor} from "../parameter.model";
import {ParameterRef, FieldNameHint, ResolvedStringValidator, FieldValueType} from "@keyscore-manager-models";
import {Field} from "../../models/value.model";

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
        readonly nameValidator: ResolvedStringValidator,
        readonly fieldValueType: FieldValueType,
        readonly mandatory: boolean
    ) {
        super(ref, displayName, description);
    }
}

export class FieldParameter extends Parameter {
    public readonly jsonClass = JSONCLASS_FIELD_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: Field
    ) {
        super(ref, value);
    }
}