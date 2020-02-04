
import {
    ListParameterDescriptor
} from "@keyscore-manager-models/src/main/parameters/parameter-lists/list-parameter.model";
import {FieldNameParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/field-name-parameter.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";
import {Serializable} from "@keyscore-manager-models/src/main/parameters/parameter.model";


export const JSONCLASS_FIELDNAMELIST_PARAM = "io.logbee.keyscore.model.configuration.FieldNameListParameter";
export const JSONCLASS_FIELDNAMELIST_DESCR = "io.logbee.keyscore.model.descriptor.FieldNameListParameterDescriptor";

export class FieldNameListParameterDescriptor extends ListParameterDescriptor {
    public readonly jsonClass = JSONCLASS_FIELDNAMELIST_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly descriptor: FieldNameParameterDescriptor,
        readonly min: number,
        readonly max: number
    ) {
        super(ref, displayName, description, descriptor, min, max);
    }
}

export class FieldNameListParameter implements Serializable {
    public readonly jsonClass = JSONCLASS_FIELDNAMELIST_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string[]
    ) {
    }
}
