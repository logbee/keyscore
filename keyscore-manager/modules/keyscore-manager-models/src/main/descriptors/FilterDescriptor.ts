import {
    ParameterDescriptorWithLocales,
    ParameterDescriptor
} from "@keyscore-manager-models";
import {TextRef} from "../common/Localization";
import {InputDescriptor} from "./InputDescriptor";
import {OutputDescriptor} from "./OutputDescriptor";
import {Icon} from "./Icon";
import {Category, CategoryWithLocales} from "./Category";
import {Ref} from "../common/Ref";


export interface FilterDescriptorWithLocales{
    name:string;
    jsonClass:FilterDescriptorJsonClass;
    displayName:TextRef;
    description:TextRef;
    categories:CategoryWithLocales[];
    parameters:ParameterDescriptorWithLocales[];
    in?:InputDescriptor;
    out?:OutputDescriptor;
    icon?:Icon;
}

export enum FilterDescriptorJsonClass{
    FilterDescriptor = "io.logbee.keyscore.model.descriptor.FilterDescriptor",
    SinkDescriptor ="io.logbee.keyscore.model.descriptor.SinkDescriptor",
    SourceDescriptor ="io.logbee.keyscore.model.descriptor.SourceDescriptor"
}

export interface FilterDescriptor{
    jsonClass:FilterDescriptorJsonClass;
    descriptorRef:Ref;
    name:string;
    displayName:string;
    description:string;
    categories:Category[];
    parameters:ParameterDescriptor[];
    in?:InputDescriptor;
    out?:OutputDescriptor;
    icon?:Icon;
}