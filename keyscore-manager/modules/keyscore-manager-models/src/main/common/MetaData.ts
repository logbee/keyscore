import {Value} from "@keyscore-manager-models/src/main/dataset/Value";

export interface MetaData{
    labels:Label[];
}

export interface Label{
    name:string;
    value:Value;
}