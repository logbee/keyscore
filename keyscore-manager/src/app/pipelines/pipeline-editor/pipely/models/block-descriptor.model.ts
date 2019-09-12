import {Connection} from "./connection.model";

import {ParameterDescriptor} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter.model";
import {Ref} from "@/../modules/keyscore-manager-models/src/main/common/Ref";
import {Icon} from "@/../modules/keyscore-manager-models/src/main/descriptors/Icon";
import {Category} from "@/../modules/keyscore-manager-models/src/main/descriptors/Category";

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
