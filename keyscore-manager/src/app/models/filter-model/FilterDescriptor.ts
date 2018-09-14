import {FilterConnection} from "./FilterConnection";
import {ParameterDescriptor} from "../pipeline-model/parameters/ParameterDescriptor";
import {TextRef} from "../common/Localization";
import {InputDescriptor} from "../descriptors/InputDescriptor";
import {OutputDescriptor} from "../descriptors/OutputDescriptor";
import {Icon} from "../descriptors/Icon";

export interface FilterDescriptor {
    name: string;
    displayName: string;
    description: string;
    previousConnection: FilterConnection;
    nextConnection: FilterConnection;
    parameters: ParameterDescriptor[];
    category: string;
}

export interface FilterDescriptorNew{
    name:string;
    displayName:TextRef;
    description:TextRef;
    categories:TextRef[];
    parameters:ParameterDescriptorNew[];
    in?:InputDescriptor;
    out?:OutputDescriptor;
    icon?:Icon;
}