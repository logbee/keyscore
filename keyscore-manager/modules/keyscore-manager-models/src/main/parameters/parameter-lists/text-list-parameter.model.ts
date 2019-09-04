import {ListParameter, ListParameterDescriptor, ParameterRef,TextParameterDescriptor} from "@keyscore-manager-models";

export const JSONCLASS_TEXTLIST_PARAM = "io.logbee.keyscore.model.configuration.TextListParameter";
export const JSONCLASS_TEXTLIST_DESCR = "io.logbee.keyscore.model.descriptor.TextListParameterDescriptor";

export class TextListParameterDescriptor extends ListParameterDescriptor {
    public readonly jsonClass = JSONCLASS_TEXTLIST_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly descriptor: TextParameterDescriptor,
        readonly min: number,
        readonly max: number
    ) {
        super(ref, displayName, description, descriptor, min, max);
    }
}

export class TextListParameter extends ListParameter {
    public jsonClass = JSONCLASS_TEXTLIST_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string[]
    ) {
        super(ref, value);
    }
}