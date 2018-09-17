import {Connection} from "./connection.model";
import {
    ResolvedParameterDescriptor
} from "../../../../models/parameters/ParameterDescriptor";

export interface BlockDescriptor {
    name: string;
    displayName: string;
    description: string;
    previousConnection: Connection;
    nextConnection: Connection;
    parameters: ResolvedParameterDescriptor[];
    category: string;
}