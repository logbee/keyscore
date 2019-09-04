import {TextRef} from "../common/Localization";

export interface CategoryWithLocales{
    name:string;
    displayName:TextRef;
}

export interface Category{
    name:string;
    displayName:string;
}