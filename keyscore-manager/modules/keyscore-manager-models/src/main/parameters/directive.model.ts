import {
    Parameter,
    ParameterDescriptor,
    Serializable
} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {ParameterRef, Ref} from "@keyscore-manager-models/src/main/common/Ref";
import {FieldValueType, ParameterInfo} from "@keyscore-manager-models/src/main/parameters/parameter-fields.model";
import {
    ParameterSet
} from "@keyscore-manager-models/src/main/common/Configuration";
import {Icon} from "@keyscore-manager-models/src/main/descriptors/Icon";

export const JSONCLASS_DIRECTIVE_SEQ_PARAM = "io.logbee.keyscore.model.configuration.FieldDirectiveSequenceParameter";
export const JSONCLASS_DIRECTIVE_SEQ_DESCR = "io.logbee.keyscore.model.descriptor.FieldDirectiveSequenceParameterDescriptor";
export const JSONCLASS_DIRECTIVE_DESCR = "io.logbee.keyscore.model.descriptor.FieldDirectiveDescriptor";


export interface DirectiveRef {
    uuid: string;
}

export interface DirectiveConfiguration {
    ref: DirectiveRef;
    instance: DirectiveRef;
    parameters: ParameterSet;
}

export interface FieldDirectiveSequenceConfiguration {
    id: string;
    parameters: ParameterSet;
    directives: DirectiveConfiguration[];
}

export class FieldDirectiveDescriptor {
    public readonly jsonClass = JSONCLASS_DIRECTIVE_DESCR;

    constructor(
        readonly ref: DirectiveRef,
        readonly displayName: string,
        readonly description:string,
        readonly parameters: ParameterDescriptor[],
        readonly icon:Icon
    ) {
    }
}

export class FieldDirectiveSequenceParameterDescriptor extends ParameterDescriptor {
    public readonly jsonClass = JSONCLASS_DIRECTIVE_SEQ_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly fieldTypes: FieldValueType,
        readonly parameters: ParameterDescriptor[],
        readonly directives: FieldDirectiveDescriptor[],
        readonly minSequences: number,
        readonly maxSequences: number
    ) {
        super(ref, displayName, description);
    }
}

export class FieldDirectiveSequenceParameter implements Serializable {
    public readonly jsonClass = JSONCLASS_DIRECTIVE_SEQ_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: FieldDirectiveSequenceConfiguration[]
    ) {
    }
}
