import {Ref} from "../common/Ref";
import {FilterDescriptor} from "./FilterDescriptor";
import {Localization} from "../common/Localization";

export interface Descriptor{
    jsonClass:string;
    ref:Ref;
    describes:FilterDescriptor;
    localization:Localization;
}