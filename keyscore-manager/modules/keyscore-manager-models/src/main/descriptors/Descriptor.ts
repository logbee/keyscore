import {FilterDescriptorWithLocales} from "@keyscore-manager-models/src/main/descriptors/FilterDescriptor";
import {Ref} from "@keyscore-manager-models/src/main/common/Ref";
import {MetaData} from "@keyscore-manager-models/src/main/common/MetaData";
import {Localization} from "@keyscore-manager-models/src/main/common/Localization";

export interface Descriptor {
    jsonClass:string;
    ref:Ref;
    describes:FilterDescriptorWithLocales;
    metadata:MetaData;
    localization:Localization;
}