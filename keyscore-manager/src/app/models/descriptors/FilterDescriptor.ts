import {ParameterDescriptor, ResolvedParameterDescriptor} from "../parameters/ParameterDescriptor";
import {TextRef} from "../common/Localization";
import {InputDescriptor} from "./InputDescriptor";
import {OutputDescriptor} from "./OutputDescriptor";
import {Icon} from "./Icon";
import {Category, ResolvedCategory} from "./Category";
import {Ref} from "../common/Ref";

export interface FilterDescriptor{
    name:string;
    displayName:TextRef;
    description:TextRef;
    categories:Category[];
    parameters:ParameterDescriptor[];
    in?:InputDescriptor;
    out?:OutputDescriptor;
    icon?:Icon;
}

export interface ResolvedFilterDescriptor{
    descriptorRef:Ref;
    name:string;
    displayName:string;
    description:string;
    categories:ResolvedCategory[];
    parameters:ResolvedParameterDescriptor[];
    in?:InputDescriptor;
    out?:OutputDescriptor;
    icon?:Icon;
}