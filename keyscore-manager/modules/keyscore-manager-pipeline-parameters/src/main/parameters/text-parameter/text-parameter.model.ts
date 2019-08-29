import {ParameterRef, ResolvedParameterInfo, ResolvedStringValidator} from "@keyscore-manager-models";
import {Parameter, ParameterDescriptor} from "../parameter.model";

export const JSONCLASS_TEXT_PARAM = "io.logbee.keyscore.model.configuration.TextParameter";
export const JSONCLASS_TEXT_DESCR="io.logbee.keyscore.model.descriptor.TextParameterDescriptor";

export class TextParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_TEXT_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly defaultValue: string,
        readonly validator: ResolvedStringValidator,
        readonly mandatory: boolean
    ) {
        super(ref, displayName, description);
    }
}

export class TextParameter extends Parameter {
    public readonly jsonClass = JSONCLASS_TEXT_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string
    ) {
        super(ref, value);
    }
}