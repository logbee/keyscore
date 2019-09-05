import {Connection} from "./connection.model";
import {ParameterDescriptor} from "@keyscore-manager-models";
import {Ref} from "@keyscore-manager-models";
import {Category} from "@keyscore-manager-models";
import {Icon} from "@keyscore-manager-models";

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
