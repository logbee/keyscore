import {FilterDescriptor} from "./FilterDescriptor";
import {Parameter} from "../pipeline-model/parameters/Parameter";

export interface FilterConfiguration {
    id: string;
    descriptor: FilterDescriptor;
    parameters: Parameter[];
}
