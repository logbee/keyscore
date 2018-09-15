import {FilterConnection} from "./FilterConnection";
import {ParameterDescriptor, ResolvedParameterDescriptor} from "../pipeline-model/parameters/ParameterDescriptor";
import {TextRef} from "../common/Localization";
import {InputDescriptor} from "../descriptors/InputDescriptor";
import {OutputDescriptor} from "../descriptors/OutputDescriptor";
import {Icon} from "../descriptors/Icon";

export interface FilterDescriptor{
    name:string;
    displayName:TextRef;
    description:TextRef;
    categories:TextRef[];
    parameters:ParameterDescriptor[];
    in?:InputDescriptor;
    out?:OutputDescriptor;
    icon?:Icon;
}

export interface ResolvedFilterDescriptor{
    name:string;
    displayName:string;
    description:string;
    categories:string[];
    parameters:ResolvedParameterDescriptor[];
    in?:InputDescriptor;
    out?:OutputDescriptor;
    icon?:Icon;
}