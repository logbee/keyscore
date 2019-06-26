import {ParameterRef, ResolvedParameterInfo} from "keyscore-manager-models";
import {Parameter, ParameterDescriptor} from "../parameter.model";

export class ExpressionParameterChoice {
    constructor(
        readonly name: string,
        readonly displayName: string,
        readonly description: string,
    ) {
    }
}

export class ExpressionParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = "io.logbee.keyscore.model.descriptor.ExpressionParameterDescriptor";

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly defaultValue: string,
        readonly choices: ExpressionParameterChoice[]
    ) {
        super(ref, displayName, description);

    }
}

export class ExpressionParameter extends Parameter {
    public readonly jsonClass = "io.logbee.keyscore.model.configuration.ExpressionParameter";

    constructor(
        readonly ref: ParameterRef,
        readonly value: string,
        readonly expressionType: string
    ) {
        super(ref, value);
    }
}
