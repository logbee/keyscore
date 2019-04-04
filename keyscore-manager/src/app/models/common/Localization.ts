
export interface Localization{
    locales: Locale[];
    mapping: Map<string,TranslationMapping>;
}

export interface TranslationMapping{
    translations: Map<string,string>;
}

export interface Locale{
    language:string;
    country:string;
}


export interface TextRef{
    id:string;
}