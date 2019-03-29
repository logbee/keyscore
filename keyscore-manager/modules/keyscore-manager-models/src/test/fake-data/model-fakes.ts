import * as faker from 'faker/locale/en_US';
import {PipelineInstance} from "../../main/pipeline-model/PipelineInstance";
import {Health} from "../../main/common/Health";
import {
    Blueprint,
    BlueprintJsonClass,
    FilterBlueprint,
    PipelineBlueprint,
    SinkBlueprint,
    SourceBlueprint
} from "../../main/blueprints/Blueprint";
import {generateParameterRef, generateRef, generateRefs} from "../../main/common/Ref";
import {Value, ValueJsonClass} from "../../main/dataset/Value";
import {Label} from "../../main/common/MetaData";
import {Configuration} from "../../main/common/Configuration";
import {Parameter, ParameterJsonClass, ParameterSet} from "../../main/parameters/Parameter";
import {Field} from "../../main/dataset/Field";
import {
    DirectiveDescriptorJsonClass,
    ExpressionType,
    FieldNameHint,
    FieldNameParameterDescriptor,
    FieldParameterDescriptor,
    FieldValueType,
    NumberRange,
    ParameterDescriptorJsonClass,
    ResolvedChoice,
    ResolvedFieldDirectiveDescriptor,
    ResolvedParameterDescriptor,
    ResolvedParameterInfo,
    ResolvedStringValidator,
    TextParameterDescriptor
} from "../../main/parameters/ParameterDescriptor";
import {EditingPipelineModel} from "../../main/pipeline-model/EditingPipelineModel";
import {Dataset} from "../../main/dataset/Dataset";
import {Record} from "../../main/dataset/Record";
import {count} from "rxjs/operators";

export const generatePipeline = (): PipelineInstance => {
    return {
        id: faker.random.uuid(),
        name: faker.system.fileName(),
        description: faker.lorem.sentence(),
        configurationId: faker.random.uuid(),
        health: getRandomHealth()
    };
};

export const generatePipelines = (count = faker.random.number({min: 1, max: 10})): PipelineInstance[] => {
    return Array.apply(null, Array(count)).map(() => generatePipeline());
};

export const generateConfigurations = (count: number = faker.random.number({min: 1, max: 10})): Configuration[] => {
    return Array.apply(null, Array(count)).map(() => generateConfiguration());
};

export const generateConfiguration = (parameterCount = faker.random.number({min: 1, max: 10})): Configuration => {
    return {
        ref: generateRef(),
        parent: generateRef(),
        parameterSet: generateParameterSet(parameterCount)
    }
};

export const generateResolvedParameterDescriptors = (count: number = faker.random.number({
    min: 1,
    max: 10
})): ResolvedParameterDescriptor[] => {
    return Array.apply(null, Array(count)).map(() => generateResolvedParameterDescriptor());
};

export const generateResolvedParameterDescriptor = (type: ParameterDescriptorJsonClass = null): ResolvedParameterDescriptor => {
    const types: ParameterDescriptorJsonClass[] = [ParameterDescriptorJsonClass.ExpressionParameterDescriptor,
        ParameterDescriptorJsonClass.ChoiceParameterDescriptor, ParameterDescriptorJsonClass.BooleanParameterDescriptor,
        ParameterDescriptorJsonClass.DecimalParameterDescriptor, ParameterDescriptorJsonClass.FieldListParameterDescriptor,
        ParameterDescriptorJsonClass.FieldNameListParameterDescriptor, ParameterDescriptorJsonClass.FieldNameParameterDescriptor,
        ParameterDescriptorJsonClass.FieldParameterDescriptor, ParameterDescriptorJsonClass.NumberParameterDescriptor,
        ParameterDescriptorJsonClass.TextListParameterDescriptor, ParameterDescriptorJsonClass.TextParameterDescriptor];
    if (type === null) {
        type = types[faker.random.number({min: 0, max: types.length - 1})];
    }

    const initialize = {
        ref: generateParameterRef(),
        info: generateInfo(),
        jsonClass: type
    };

    switch (type) {
        case ParameterDescriptorJsonClass.TextParameterDescriptor:
            return {
                ...initialize,
                defaultValue: faker.random.word(),
                validator: generateValidator(),
                mandatory: faker.random.boolean()
            };
        case ParameterDescriptorJsonClass.ExpressionParameterDescriptor:
            return {
                ...initialize,
                defaultValue: ".*",
                expressionType: ExpressionType.RegEx,
                mandatory: faker.random.boolean()
            };
        case ParameterDescriptorJsonClass.NumberParameterDescriptor:
            let range = generateNumberRange();
            return {
                ...initialize,
                defaultValue: faker.random.number({min: range.start, max: range.end}),
                range: range,
                mandatory: faker.random.boolean()
            };
        case ParameterDescriptorJsonClass.DecimalParameterDescriptor:
            let rangeDecimal = generateNumberRange();
            return {
                ...initialize,
                defaultValue: faker.random.number({min: rangeDecimal.start, max: rangeDecimal.end}),
                range: rangeDecimal,
                decimals: faker.random.number({min: 1, max: 2}),
                mandatory: faker.random.boolean()
            };
        case ParameterDescriptorJsonClass.FieldParameterDescriptor:
            return {
                ...initialize,
                defaultName: faker.random.word(),
                hint: generateFieldNameHint(),
                nameValidator: generateValidator(),
                fieldValueType: generateFieldValueType(),
                mandatory: faker.random.boolean()
            };
        case ParameterDescriptorJsonClass.FieldNameParameterDescriptor:
            return {
                ...initialize,
                defaultValue: faker.random.word(),
                hint: FieldNameHint.PresentField,
                validator: generateValidator(),
                mandatory: faker.random.boolean()
            };
        case ParameterDescriptorJsonClass.TextListParameterDescriptor:
            return {
                ...initialize,
                descriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.TextParameterDescriptor) as TextParameterDescriptor,
                min: faker.random.number({min: 1, max: 10}),
                max: faker.random.number({min: 11, max: 20})
            };
        case ParameterDescriptorJsonClass.FieldNameListParameterDescriptor:
            return {
                ...initialize,
                descriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.FieldNameParameterDescriptor) as FieldNameParameterDescriptor,
                min: faker.random.number({min: 1, max: 10}),
                max: faker.random.number({min: 11, max: 20})
            };
        case ParameterDescriptorJsonClass.FieldListParameterDescriptor:
            return {
                ...initialize,
                descriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.FieldParameterDescriptor) as FieldParameterDescriptor,
                min: faker.random.number({min: 1, max: 10}),
                max: faker.random.number({min: 11, max: 20})
            };
        case  ParameterDescriptorJsonClass.ChoiceParameterDescriptor:
            return {
                ...initialize,
                min: faker.random.number({min: 1, max: 10}),
                max: faker.random.number({min: 11, max: 20}),
                choices: generateResolvedChoices()
            };
        case ParameterDescriptorJsonClass.BooleanParameterDescriptor:
            return {
                ...initialize,
                defaultValue: faker.random.boolean(),
                mandatory: faker.random.boolean()
            };
        case ParameterDescriptorJsonClass.FieldDirectiveSequenceParameterDescriptor:
            return {
                ...initialize,
                fieldTypes: FieldValueType.Text,
                parameters: generateResolvedParameterDescriptors(),
                directives: generateResolvedFieldDirectiveDescriptors(),
                minSequences: 0,
                maxSequences: 100
            }

    }
};

export const generateResolvedFieldDirectiveDescriptors = (count = faker.random.number({min: 1, max: 10})) => {
    return Array.apply(null, Array(count)).map(() => generateResolvedFieldDirectiveDescriptor());
};

export const generateResolvedFieldDirectiveDescriptor = (): ResolvedFieldDirectiveDescriptor => {
    return {
        ref: generateRef(),
        info: generateInfo(),
        jsonClass: DirectiveDescriptorJsonClass.FieldDirectiveDescriptor,
        parameters: generateResolvedParameterDescriptors(faker.random.number({min:1,max:4})),
        minSequences: 0,
        maxSequences: 100
    }
};

export const generateResolvedChoices = (count = faker.random.number({min: 1, max: 10})) => {
    return Array.apply(null, Array(count)).map(() => generateResolvedChoice());
};

export const generateResolvedChoice = (): ResolvedChoice => {
    return {
        name: faker.random.uuid(),
        displayName: faker.random.word(),
        description: faker.lorem.sentence()
    }
};

export const generateFieldValueType = (): FieldValueType => {
    const types = [FieldValueType.Boolean, FieldValueType.Decimal, FieldValueType.Duration, FieldValueType.Number,
        FieldValueType.Text, FieldValueType.Timestamp, FieldValueType.Unknown];

    return types[faker.random.number({min: 0, max: types.length - 1})];

};
export const generateFieldNameHint = (): FieldNameHint => {
    const types = [FieldNameHint.AbsentField, FieldNameHint.PresentField, FieldNameHint.AnyField];

    return types[faker.random.number({min: 0, max: types.length - 1})];
};

export const generateNumberRange = (): NumberRange => {
    return {
        step: faker.random.number({min: 1, max: 10}),
        start: faker.random.number({min: 1, max: 10}),
        end: faker.random.number({min: 11, max: 30})
    };
};

export const generateValidator = (): ResolvedStringValidator => {
    return {
        expression: ".*",
        expressionType: ExpressionType.RegEx,
        description: "Everything is fine here"
    };
};

export const generateInfo = (): ResolvedParameterInfo => {
    return {
        displayName: faker.random.word(),
        description: faker.lorem.sentence()
    };
};

export const generateParameterSet = (count: number = faker.random.number({min: 1, max: 10})): ParameterSet => {
    return {jsonClass: ParameterJsonClass.ParameterSet, parameters: generateParameters(count)};
};

export const generateParameters = (count: number = faker.random.number({min: 1, max: 10})): Parameter[] => {
    return Array.apply(null, Array(count)).map(() => generateParameter());
};

export const generateParameter = (type: ParameterJsonClass = null): Parameter => {
    const types = [ParameterJsonClass.BooleanParameter, ParameterJsonClass.TextParameter,
        ParameterJsonClass.ExpressionParameter, ParameterJsonClass.NumberParameter,
        ParameterJsonClass.DecimalParameter, ParameterJsonClass.FieldNameParameter, ParameterJsonClass.FieldParameter,
        ParameterJsonClass.TextListParameter, ParameterJsonClass.FieldNameListParameter,
        ParameterJsonClass.FieldListParameter, ParameterJsonClass.ChoiceParameter];
    if (type === null) {
        type = types[faker.random.number({min: 0, max: types.length - 1})];
    }
    const initialization = {ref: generateParameterRef(), jsonClass: type};

    switch (type) {
        case ParameterJsonClass.BooleanParameter:
            return {
                ...initialization,
                value: faker.random.boolean()
            };
        case ParameterJsonClass.TextParameter:
        case ParameterJsonClass.ChoiceParameter:
        case ParameterJsonClass.ExpressionParameter:
        case ParameterJsonClass.FieldNameParameter:
            return {
                ...initialization,
                value: faker.random.word()
            };
        case ParameterJsonClass.NumberParameter:
        case ParameterJsonClass.DecimalParameter:
            return {
                ...initialization,
                value: faker.random.number()
            };
        case ParameterJsonClass.FieldNameListParameter:
        case ParameterJsonClass.TextListParameter:
            return {
                ...initialization,
                value: generateWordList()
            };
        case ParameterJsonClass.FieldParameter:
            return {
                ...initialization,
                value: generateField()
            };
        case ParameterJsonClass.FieldListParameter:
            return {
                ...initialization,
                value: generateFields()
            }
    }
};

export const generateEditingPipelineModel = (count: number = faker.random.number({
    min: 1,
    max: 10
})): EditingPipelineModel => {
    return {
        pipelineBlueprint: generatePipelineBlueprint(count),
        blueprints: generateBlueprints(count),
        configurations: generateConfigurations(count)
    }
};

export const generatePipelineBlueprint = (blueprintCount: number = faker.random.number({
    min: 1,
    max: 10
})): PipelineBlueprint => {
    return {
        ref: {
            uuid: faker.random.uuid()
        },
        blueprints: generateRefs(blueprintCount),
        metadata: {
            labels: generateLabels()
        }
    }
};

export const generateBlueprints = (count: number = faker.random.number({min: 1, max: 10})): Blueprint[] => {

    return Array.apply(null, Array(count)).map((value, index, array) =>
        index === 0 ? generateBlueprint(BlueprintJsonClass.SourceBlueprint) :
            index === count - 1 ? generateBlueprint(BlueprintJsonClass.SinkBlueprint) :
                generateBlueprint(BlueprintJsonClass.FilterBlueprint));
};

export const generateBlueprint = (type: BlueprintJsonClass = null): Blueprint => {
    const types = [BlueprintJsonClass.SinkBlueprint, BlueprintJsonClass.SourceBlueprint, BlueprintJsonClass.FilterBlueprint];
    if (type === null) {
        type = types[faker.random.number({min: 0, max: types.length - 1})];
    }
    switch (type) {
        case BlueprintJsonClass.FilterBlueprint:
            return generateFilterBlueprint();
        case BlueprintJsonClass.SinkBlueprint:
            return generateSinkBlueprint();
        case BlueprintJsonClass.SourceBlueprint:
            return generateSourceBlueprint();
        default:
            return generateFilterBlueprint();
    }
};

export const generateSinkBlueprint = (): SinkBlueprint => {
    return {
        jsonClass: BlueprintJsonClass.SinkBlueprint,
        ref: generateRef(),
        descriptor: generateRef(),
        configuration: generateRef(),
        in: generateRef()
    }
};

export const generateFilterBlueprint = (): FilterBlueprint => {
    return {
        jsonClass: BlueprintJsonClass.FilterBlueprint,
        ref: generateRef(),
        descriptor: generateRef(),
        configuration: generateRef(),
        in: generateRef(),
        out: generateRef()
    }
};

export const generateSourceBlueprint = (): SourceBlueprint => {
    return {
        jsonClass: BlueprintJsonClass.SourceBlueprint,
        ref: generateRef(),
        descriptor: generateRef(),
        configuration: generateRef(),
        out: generateRef()
    }
};

export const generateDatasets = (count: number = faker.random.number({min: 1, max: 10})): Dataset[] => {
    return Array.apply(null, Array(count)).map(() => generateDataset())
};

export const generateDataset = (recordCount: number = faker.random.number({min: 1, max: 10})): Dataset => {
    return {
        metaData: null,
        records: generateRecords(recordCount)
    }
};

export const generateRecords = (count: number = faker.random.number({min: 1, max: 10})): Record[] => {
    return Array.apply(null, Array(count)).map(() => generateRecord())
};

export const generateRecord = (fieldCount: number = faker.random.number({min: 1, max: 10})): Record => {
    return {
        fields: generateFields(fieldCount)
    }
};

export const generateValue = (): Value => {
    return {
        jsonClass: ValueJsonClass.TextValue,
        value: faker.random.word()
    }
};

export const generateField = (): Field => {
    return {
        name: faker.random.word(),
        value: generateValue()
    }
};

export const generateFields = (count: number = faker.random.number({min: 1, max: 10})): Field[] => {
    return Array.apply(null, Array(count)).map(() => generateField());
};

export const generateLabel = (): Label => {
    return {
        name: faker.random.uuid(),
        value: generateValue()
    }
};

export const generateLabels = (count: number = faker.random.number({min: 1, max: 10})): Label[] => {
    return Array.apply(null, Array(count)).map(() => generateLabel());
};

function getRandomHealth(): Health {
    const healthArray: Health[] = [Health.Green, Health.Red, Health.Yellow];
    return healthArray[faker.random.number({min: 0, max: 3})];
}

export const generateWordList = (count = faker.random.number({min: 1, max: 10})): string[] => {
    return Array.apply(null, Array(count)).map(() => faker.random.word());
};

