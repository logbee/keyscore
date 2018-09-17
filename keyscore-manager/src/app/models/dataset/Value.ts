export interface BooleanValue{
    jsonClass: string;
    value:boolean;
}

export interface TextValue {
    jsonClass: string;
    value: string;
}

export interface NumberValue {
    jsonClass: string;
    value: number;
}

export interface DecimalValue {
    jsonClass: string;
    value: number;
}

export interface TimestampValue {
    jsonClass: string;
    seconds: string;
    nanos: string;
}

export interface DurationValue {
    jsonClass: string;
    seconds: string;
    nanos: string;
}

export type Value =
    | TextValue
    | DecimalValue
    | DurationValue
    | TimestampValue
    | NumberValue
    | BooleanValue;
