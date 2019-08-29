import {
    ParameterRef,
    ListParameter,
    ListParameterDescriptor,
    FieldNameParameterDescriptor
} from "@keyscore-manager-models";


export const JSONCLASS_FIELDNAMELIST_PARAM = "io.logbee.keyscore.model.configuration.FieldNameListParameter";
export const JSONCLASS_FIELDNAMELIST_DESCR = "io.logbee.keyscore.model.descriptor.FieldNameListParameterDescriptor";

export class FieldNameListParameterDescriptor extends ListParameterDescriptor {
    public readonly jsonClass = JSONCLASS_FIELDNAMELIST_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly descriptor: FieldNameParameterDescriptor,
        readonly min: number,
        readonly max: number
    ) {
        super(ref, displayName, description, descriptor, min, max);
    }
}

export class FieldNameListParameter extends ListParameter {
    public jsonClass = JSONCLASS_FIELDNAMELIST_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string[]
    ) {
        super(ref, value);
    }
}