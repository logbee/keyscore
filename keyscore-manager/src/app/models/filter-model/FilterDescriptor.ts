import {FilterConnection} from "./FilterConnection";
import {ParameterDescriptor} from "../pipeline-model/parameters/ParameterDescriptor";

export interface FilterDescriptor {
    name: string;
    displayName: string;
    description: string;
    previousConnection: FilterConnection;
    nextConnection: FilterConnection;
    parameters: ParameterDescriptor[];
    category: string;
}