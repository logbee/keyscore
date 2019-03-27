export enum ValueJsonClass {

    BooleanValue = "io.logbee.keyscore.model.data.BooleanValue",
    TextValue = "io.logbee.keyscore.model.data.TextValue",
    NumberValue = "io.logbee.keyscore.model.data.NumberValue",
    DecimalValue = "io.logbee.keyscore.model.data.DecimalValue",
    TimestampValue = "io.logbee.keyscore.model.data.TimestampValue",
    DurationValue = "io.logbee.keyscore.model.data.DurationValue"
}


export interface BooleanValue {
    jsonClass: ValueJsonClass.BooleanValue;
    value: boolean;
}

export interface TextValue {
    jsonClass: ValueJsonClass.TextValue;
    value: string;
}

export interface NumberValue {
    jsonClass: ValueJsonClass.NumberValue;
    value: number;
}

export interface DecimalValue {
    jsonClass: ValueJsonClass.DecimalValue;
    value: number;
}

export interface TimestampValue {
    jsonClass: ValueJsonClass.TimestampValue;
    seconds: string;
    nanos: string;
}

export interface DurationValue {
    jsonClass: ValueJsonClass.DurationValue;
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