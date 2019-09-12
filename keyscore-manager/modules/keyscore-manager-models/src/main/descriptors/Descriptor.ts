import {Localization, MetaData, Ref} from "@keyscore-manager-models/src/main/common";
import {FilterDescriptorWithLocales} from "@keyscore-manager-models/src/main/descriptors/FilterDescriptor";

export interface Descriptor {
    jsonClass:string;
    ref:Ref;
    describes:FilterDescriptorWithLocales;
    metadata:MetaData;
    localization:Localization;
}