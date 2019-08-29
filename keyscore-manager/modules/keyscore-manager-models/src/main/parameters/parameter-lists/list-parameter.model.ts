import {Parameter, ParameterDescriptor} from "../parameter.model";
import {ParameterRef} from "@keyscore-manager-models";


export abstract class ListParameterDescriptor extends ParameterDescriptor {
    protected constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly descriptor: ParameterDescriptor,
        readonly min: number,
        readonly max: number
    ) {
        super(ref, displayName, description);
    }
}

export abstract class ListParameter extends Parameter {
    protected constructor(
        readonly ref: ParameterRef,
        readonly value: any[]
    ) {
        super(ref, value);
    }
}

