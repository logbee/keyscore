import {Ref} from "../common/Ref";
import {FilterDescriptor} from "./FilterDescriptor";
import {Localization} from "../common/Localization";
import {MetaData} from "../common/MetaData";

export interface Descriptor{
    jsonClass:string;
    ref:Ref;
    describes:FilterDescriptor;
    metadata:MetaData;
    localization:Localization;
}