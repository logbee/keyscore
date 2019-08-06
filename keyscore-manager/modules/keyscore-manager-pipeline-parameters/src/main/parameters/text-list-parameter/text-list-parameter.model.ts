import {Parameter, ParameterDescriptor} from "../parameter.model";
import {ParameterRef} from "keyscore-manager-models";
import {TextParameterDescriptor} from "../text-parameter/text-parameter.model";


export abstract class ListParameterDescriptor extends ParameterDescriptor {
    protected constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly descriptor: ParameterDescriptor,
        readonly min: number,
        readonly max: number
    ) {
        super(ref, displayName, description);
    }
}

export abstract class ListParameter extends Parameter {
    protected constructor(
        readonly ref: ParameterRef,
        readonly value: any[]
    ) {
        super(ref, value);
    }
}

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

