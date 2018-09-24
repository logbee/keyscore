import {Parameter} from "../../../../models/parameters/Parameter";
import {BlockDescriptor} from "./block-descriptor.model";
import {Ref} from "../../../../models/common/Ref";

export interface BlockConfiguration {
    ref: Ref;
    descriptor:BlockDescriptor;
    parameters: Parameter[];
}