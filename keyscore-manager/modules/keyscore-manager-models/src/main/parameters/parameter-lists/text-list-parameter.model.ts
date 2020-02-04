import {
    ListParameterDescriptor
} from "@keyscore-manager-models/src/main/parameters/parameter-lists/list-parameter.model";
import {TextParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/text-parameter.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";
import {Serializable} from "@keyscore-manager-models/src/main/parameters/parameter.model";

export const JSONCLASS_TEXTLIST_PARAM = "io.logbee.keyscore.model.configuration.TextListParameter";
export const JSONCLASS_TEXTLIST_DESCR = "io.logbee.keyscore.model.descriptor.TextListParameterDescriptor";

export class TextListParameterDescriptor extends ListParameterDescriptor {
    public readonly jsonClass = JSONCLASS_TEXTLIST_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly descriptor: TextParameterDescriptor,
        readonly min: number,
        readonly max: number
    ) {
        super(ref, displayName, description, descriptor, min, max);
    }
}

export class TextListParameter implements Serializable{
    public readonly jsonClass = JSONCLASS_TEXTLIST_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: string[]
    ) {
    }
}
