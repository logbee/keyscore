import {ParameterRef, ResolvedParameterInfo, ResolvedStringValidator} from 'keyscore-manager-models';
import {Parameter, ParameterDescriptor} from "../parameter.model";

export class TextParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = "io.logbee.keyscore.model.descriptor.TextParameterDescriptor";

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
    public readonly jsonClass = "io.logbee.keyscore.model.configuration.TextParameter";

    constructor(
        readonly ref: ParameterRef,
        readonly value: string
    ) {
        super(ref, value);
    }
}