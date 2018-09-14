import {Ref} from "../common/Ref";
import {FilterDescriptorNew} from "../filter-model/FilterDescriptor";
import {Localization} from "../common/Localization";

export interface Descriptor{
    jsonClass:string;
    ref:Ref;
    describes:FilterDescriptorNew;
    localization:Localization;
}