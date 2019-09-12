import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";

export const JSONCLASS_EXPRESSION_PARAM = "io.logbee.keyscore.model.configuration.ExpressionParameter";
export const JSONCLASS_EXPRESSION_DESCR = "io.logbee.keyscore.model.descriptor.ExpressionParameterDescriptor";

export class ExpressionParameterChoice {
    constructor(
        readonly name: string,
        readonly displayName: string,
        readonly description: string,
    ) {
    }
}

export class ExpressionParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_EXPRESSION_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly defaultValue: string,
        readonly mandatory: boolean,
        readonly choices: ExpressionParameterChoice[]
    ) {
        super(ref, displayName, description);

    }
}

export class ExpressionParameter extends Parameter {
    public readonly jsonClass = JSONCLASS_EXPRESSION_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string,
        readonly expressionType: string
    ) {
        super(ref, value);
    }
}
