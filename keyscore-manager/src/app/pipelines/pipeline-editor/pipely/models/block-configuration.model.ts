import {Parameter} from "../../../../models/parameters/Parameter";
import {BlockDescriptor} from "./block-descriptor.model";

export interface BlockConfiguration {
    id: string;
    descriptor:BlockDescriptor;
    parameters: Parameter[];
}