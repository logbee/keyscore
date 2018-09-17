import {TextRef} from "../common/Localization";

export interface Category{
    name:string;
    displayName:TextRef;
}

export interface ResolvedCategory{
    name:string;
    displayName:string;
}