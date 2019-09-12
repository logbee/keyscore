import {Category, CategoryWithLocales} from "@keyscore-manager-models/src/main/descriptors/Category";
import {InputDescriptor} from "@keyscore-manager-models/src/main/descriptors/InputDescriptor";
import {OutputDescriptor} from "@keyscore-manager-models/src/main/descriptors/OutputDescriptor";
import {Icon} from "@keyscore-manager-models/src/main/descriptors/Icon";
import {ParameterDescriptorWithLocales} from "@keyscore-manager-models/src/main/parameters/parameter-fields.model";
import {ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {TextRef} from "@keyscore-manager-models/src/main/common/Localization";
import {Ref} from "@keyscore-manager-models/src/main/common/Ref";


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