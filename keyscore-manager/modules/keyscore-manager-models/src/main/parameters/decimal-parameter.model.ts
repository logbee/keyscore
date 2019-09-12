import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {NumberRange} from "@keyscore-manager-models/src/main/parameters/parameter-fields.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";

export const JSONCLASS_DECIMAL_PARAM = "io.logbee.keyscore.model.configuration.DecimalParameter";
export const JSONCLASS_DECIMAL_DESCR = "io.logbee.keyscore.model.descriptor.DecimalParameterDescriptor";

export class DecimalParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_DECIMAL_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly defaultValue: number,
        readonly range: NumberRange,
        readonly decimals: number,
        readonly mandatory: boolean
    ) {
        super(ref, displayName, description);
    }

}

export class DecimalParameter extends Parameter {
    public readonly jsonClass = JSONCLASS_DECIMAL_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: number
    ) {
        super(ref, value);
    }
}