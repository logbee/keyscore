export interface ValueI {
    readonly jsonClass: string;
}

export enum ValueJsonClass {
    BooleanValue = "io.logbee.keyscore.model.data.BooleanValue",
    TextValue = "io.logbee.keyscore.model.data.TextValue",
    NumberValue = "io.logbee.keyscore.model.data.NumberValue",
    DecimalValue = "io.logbee.keyscore.model.data.DecimalValue",
    TimestampValue = "io.logbee.keyscore.model.data.TimestampValue",
    DurationValue = "io.logbee.keyscore.model.data.DurationValue",
    BinaryValue = "io.logbee.keyscore.model.data.BinaryValue"
}

export class BooleanValue implements ValueI {
    readonly jsonClass = ValueJsonClass.BooleanValue;

    constructor(
        readonly value: boolean
    ) {
    }
}

export class TextValue implements ValueI {
    readonly jsonClass = ValueJsonClass.TextValue;

    constructor(
        readonly value: string
    ) {
    }
}

export class NumberValue implements ValueI {
    readonly jsonClass = ValueJsonClass.NumberValue;

    constructor(
        readonly value: number
    ) {
    }
}


export class DecimalValue implements ValueI {
    readonly jsonClass = ValueJsonClass.DecimalValue;

    constructor(
        readonly value: number
    ) {
    }
}


export class TimestampValue implements ValueI {
    readonly jsonClass = ValueJsonClass.TimestampValue;

    constructor(
        readonly seconds: number,
        readonly nanos: number
    ) {
    }
}


export class DurationValue implements ValueI {
    readonly jsonClass = ValueJsonClass.DurationValue;

    constructor(
        readonly seconds: number,
        readonly nanos: number
    ) {
    }
}

export class BinaryValue implements ValueI {
    readonly jsonClass = ValueJsonClass.BinaryValue;

    constructor(
        readonly value: Uint8Array,
        readonly byteOrder: string
    ) {
    }
}

export type Value = BooleanValue
    | TextValue
    | NumberValue
    | DecimalValue
    | TimestampValue
    | DurationValue
    | BinaryValue;