import {Parameter} from "../../../../models/pipeline-model/parameters/Parameter";
import {BlockDescriptor} from "./block-descriptor.model";

export interface BlockConfiguration {
    id: string;
    descriptor: BlockDescriptor;
    parameters: Parameter[];
}