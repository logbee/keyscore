export interface Icon {
    data: string;
    format: IconFormat;
    encoding: IconEncoding;
}

export enum IconFormat{
    SVG = 0
}

export enum IconEncoding{
    RAW=0,
    Base64=1
}
