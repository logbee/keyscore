export class ExpressionParameterChoice {
    constructor(
        readonly name: string,
        readonly displayName: string,
        readonly description: string,
    ) {}
}

export class ExpressionParameterDescriptor {
    public readonly jsonClass = "io.logbee.keyscore.model.descriptor.ExpressionParameterDescriptor";
    constructor(
        readonly ref: string,
        readonly displayName: string,
        readonly description: string,
        readonly defaultValue: string,
        readonly choices: ExpressionParameterChoice[]
    ) {}
}

export class ExpressionParameter {
    public readonly jsonClass = "io.logbee.keyscore.model.configuration.ExpressionParameter";
    constructor(
        readonly ref: string,
        readonly value: string,
        readonly expressionType: string
    ) {}
}
