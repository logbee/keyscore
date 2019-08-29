export abstract class Value {
    readonly jsonClass: string;
}

export class BooleanValue extends Value {
    readonly jsonClass = "io.logbee.keyscore.model.data.BooleanValue";

    constructor(
        readonly value: boolean
    ) {
        super();
    }
}

export class TextValue extends Value {
    readonly jsonClass = "io.logbee.keyscore.model.data.TextValue";

    constructor(
        readonly value: string
    ) {
        super();
    }
}

export class NumberValue extends Value {
    readonly jsonClass = "io.logbee.keyscore.model.data.NumberValue";

    constructor(
        readonly value: number
    ) {
        super();
    }
}

export class DecimalValue extends Value {
    readonly jsonClass = "io.logbee.keyscore.model.data.DecimalValue";

    constructor(
        readonly value: number
    ) {
        super();
    }
}

export class TimestampValue extends Value {
    readonly jsonClass = "io.logbee.keyscore.model.data.TimestampValue";

    constructor(
        readonly seconds: number,
        readonly nanos: number
    ) {
        super();
    }
}

export class DurationValue extends Value {
    readonly jsonClass = "io.logbee.keyscore.model.data.DurationValue";

    constructor(
        readonly seconds: number,
        readonly nanos: number
    ) {
        super();
    }
}