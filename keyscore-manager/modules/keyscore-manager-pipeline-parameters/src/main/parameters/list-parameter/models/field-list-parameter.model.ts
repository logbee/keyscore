import {ListParameter, ListParameterDescriptor} from "./list-parameter.model";
import {ParameterRef} from "keyscore-manager-models";
import {FieldParameterDescriptor} from "../../field-parameter/field-parameter.model";
import {Field} from "../../../models/value.model";


export const JSONCLASS_FIELDLIST_PARAM = "io.logbee.keyscore.model.configuration.FieldListParameter";
export const JSONCLASS_FIELDLIST_DESCR = "io.logbee.keyscore.model.descriptor.FieldListParameterDescriptor";

export class FieldListParameterDescriptor extends ListParameterDescriptor {
    public readonly jsonClass = JSONCLASS_FIELDLIST_DESCR;

    constructor(
        readonly ref: ParameterRef,
        readonly displayName: string,
        readonly description: string,
        readonly descriptor: FieldParameterDescriptor,
        readonly min: number,
        readonly max: number
    ) {
        super(ref, displayName, description, descriptor, min, max);
    }
}

export class FieldListParameter extends ListParameter {
    public jsonClass = JSONCLASS_FIELDLIST_PARAM;

    constructor(
        readonly ref: ParameterRef,
        readonly value: Field[]
    ) {
        super(ref, value);
    }
}