import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";

export const JSONCLASS_GROUP_PARAM = "io.logbee.keyscore.model.configuration.ParameterGroup";
export const JSONCLASS_GROUP_DESCR = "io.logbee.keyscore.model.descriptor.ParameterGroupDescriptor";

export enum ParameterGroupConditionJsonClass{
    BooleanCondition = "io.logbee.keyscore.model.descriptor.BooleanParameterCondition"
}

export interface IParameterGroupCondition{
    jsonClass:string;
    parameter:ParameterRef;
    negate?:boolean;
}

export abstract class ParameterGroupCondition {
    public readonly jsonClass: string;

    constructor(
        readonly parameter: ParameterRef
    ) {
    }
}

export class BooleanParameterCondition extends ParameterGroupCondition {
    public readonly jsonClass = ParameterGroupConditionJsonClass.BooleanCondition;

    constructor(
        readonly parameter: ParameterRef,
        readonly negate: boolean
    ) {
        super(parameter);
    }

}

export class ParameterGroupDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_GROUP_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly condition: ParameterGroupCondition,
        readonly parameters: ParameterDescriptor[]
    ) {
        super(ref, displayName, description);
    }
}

export class ParameterGroup extends Parameter {
    public readonly jsonClass = JSONCLASS_GROUP_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: Parameter[]
    ) {
        super(ref, value);
    }
}
