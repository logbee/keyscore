import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";


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

/*export abstract class ListParameter extends Parameter {
    public readonly jsonClass: string = "list_parameter";

    protected constructor(
        readonly ref: ParameterRef,
        readonly value: any[]
    ) {
        super(ref, value);
    }
}*/

