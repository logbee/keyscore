import {Connection} from "./connection.model";
import {ParameterDescriptor} from "../../../../models/pipeline-model/parameters/ParameterDescriptor";

export interface BlockDescriptor {
    name: string;
    displayName: string;
    description: string;
    previousConnection: Connection;
    nextConnection: Connection;
    parameters: ParameterDescriptor[];
    category: string;
}