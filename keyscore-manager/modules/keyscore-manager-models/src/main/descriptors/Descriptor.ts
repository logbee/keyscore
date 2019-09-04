import {Ref} from "../common/Ref";
import {FilterDescriptorWithLocales} from "./FilterDescriptor";
import {Localization} from "../common/Localization";
import {MetaData} from "../common/MetaData";

export interface Descriptor {
    jsonClass:string;
    ref:Ref;
    describes:FilterDescriptorWithLocales;
    metadata:MetaData;
    localization:Localization;
}