import {Value} from "../dataset/Value";

export interface MetaData{
    labels:Label[];
}

export interface Label{
    name:string;
    value:Value;
}