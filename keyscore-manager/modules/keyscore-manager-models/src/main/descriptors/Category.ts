import {TextRef} from "@keyscore-manager-models/src/main/common";

export interface CategoryWithLocales{
    name:string;
    displayName:TextRef;
}

export interface Category{
    name:string;
    displayName:string;
}