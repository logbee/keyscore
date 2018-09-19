import * as faker from 'faker/locale/en_US';
import {PipelineInstance} from "../../app/models/pipeline-model/PipelineInstance";
import {Health} from "../../app/models/common/Health";
import {
    Blueprint, BlueprintJsonClass, FilterBlueprint, PipelineBlueprint,
    SinkBlueprint, SourceBlueprint
} from "../../app/models/blueprints/Blueprint";
import {ParameterRef, Ref} from "../../app/models/common/Ref";
import {Value} from "../../app/models/dataset/Value";
import {count} from "rxjs/internal/operators";
import {Label} from "../../app/models/common/MetaData";
import {Configuration} from "../../app/models/common/Configuration";
import {Parameter, ParameterJsonClass} from "../../app/models/parameters/Parameter";
import {Field} from "../../app/models/dataset/Field";
import {ParameterDescriptor, ParameterDescriptorJsonClass} from "../../app/models/parameters/ParameterDescriptor";

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
        parameters: generateParameters(parameterCount)
    }
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
    const initialization = {ref: generateRef(), jsonClass: type};

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

export const generateRef = (): Ref => {
    return {
        uuid: faker.random.uuid()
    }
};

export const generateRefs = (count = faker.random.number({min: 1, max: 10})): Ref[] => {
    return Array.apply(null, Array(count)).map(() => generateRef());
};

export const generateParameterRef = (): ParameterRef => {
    return {
        uuid: faker.random.uuid()
    }
};

export const generateParameterRefs = (count = faker.random.number({min: 1, max: 10})): ParameterRef[] => {
    return Array.apply(null, Array(count)).map(() => generateParameterRef());
};

export const generateValue = (): Value => {
    return {
        jsonClass: "TextValue",
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
}

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
};

function generateWordList(count = faker.random.number({min: 1, max: 10})): string[] {
    return Array.apply(null, Array(count)).map(() => faker.random.word());
}

