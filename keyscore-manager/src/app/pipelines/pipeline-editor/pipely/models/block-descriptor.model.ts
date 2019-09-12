import {Connection} from "./connection.model";
import {Ref} from "@keyscore-manager-models";
import {Category} from "@keyscore-manager-models";
import {Icon} from "@keyscore-manager-models";
import {ParameterDescriptor} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter.model";

export interface BlockDescriptor {
    ref: Ref;
    displayName: string;
    description: string;
    previousConnection: Connection;
    nextConnection: Connection;
    parameters: ParameterDescriptor[];
    categories: Category[];
    icon?: Icon;
}
