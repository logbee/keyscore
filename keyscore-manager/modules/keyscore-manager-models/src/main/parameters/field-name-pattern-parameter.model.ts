import {FieldNameHint, PatternType} from "@keyscore-manager-models/src/main/parameters/parameter-fields.model";
import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {ParameterRef} from "@keyscore-manager-models";

export const JSONCLASS_FIELDNAMEPATTERN_PARAM = "io.logbee.keyscore.model.configuration.FieldNamePatternParameter";
export const JSONCLASS_FIELDNAMEPATTERN_DESCR = "io.logbee.keyscore.model.descriptor.FieldNamePatternParameterDescriptor";


export class PatternTypeChoice {

    private constructor(
        readonly type: PatternType,
        readonly displayName: string
    ) {
    }

    public static fromPatternType(type: PatternType): PatternTypeChoice {
        switch (type) {
            case PatternType.RegEx:
                return new PatternTypeChoice(type, "Regular Expression");
            case PatternType.Glob:
                return new PatternTypeChoice(type, "Glob Expression");
            default:
                return new PatternTypeChoice(PatternType.None, "None");

        }
    }
}

export class FieldNamePatternParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_FIELDNAMEPATTERN_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly defaultValue: string,
        readonly hint: FieldNameHint,
        readonly supports: PatternTypeChoice[],
        readonly mandatory: boolean
    ) {
        super(ref, displayName, description);
    }
}

export class FieldNamePatternParameter extends Parameter {
    public readonly jsonClass = JSONCLASS_FIELDNAMEPATTERN_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string,
        readonly patternType: PatternType
    ) {
        super(ref, value);
    }
}