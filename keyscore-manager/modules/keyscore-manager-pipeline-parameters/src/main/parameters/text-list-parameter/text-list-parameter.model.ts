import {Parameter, ParameterDescriptor} from "../parameter.model";
import {ParameterRef} from "keyscore-manager-models";
import {TextParameterDescriptor} from "../text-parameter/text-parameter.model";


export const JSONCLASS_TEXTLIST_PARAM = "io.logbee.keyscore.model.configuration.TextListParameter";
export const JSONCLASS_TEXTLIST_DESCR = "io.logbee.keyscore.model.descriptor.TextListParameterDescriptor";

export class TextListParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_TEXTLIST_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly descriptor: TextParameterDescriptor,
        readonly min: number,
        readonly max: number
    ) {
        super(ref, displayName, description);
    }
}

export class TextListParameter extends Parameter {
    public jsonClass = JSONCLASS_TEXTLIST_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string[]
    ) {
        super(ref, value);
    }
}
