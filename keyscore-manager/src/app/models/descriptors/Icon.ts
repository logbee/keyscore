export interface Icon {
    data: string;
    format: IconFormat;
    encoding: IconEncoding;
}

export enum IconFormat{
    SVG = "SVG"
}

export enum IconEncoding{
    RAW="RAW",
    Base64="Base64"
}
