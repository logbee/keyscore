import * as faker from 'faker/locale/en_US';
import {PipelineInstance} from "../../app/models/pipeline-model/PipelineInstance";
import {Health} from "../../app/models/common/Health";
import {PipelineBlueprint} from "../../app/models/blueprints/Blueprint";
import {Ref} from "../../app/models/common/Ref";
import {Value} from "../../app/models/dataset/Value";
import {count} from "rxjs/internal/operators";
import {Label} from "../../app/models/common/MetaData";

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

export const generatePipelineBlueprint = (): PipelineBlueprint => {
    return {
        ref: {
            uuid: faker.random.uuid()
        },
        blueprints: generateRefs(),
        metadata: {
            labels: generateLabels()
        }
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

export const generateValue = (): Value => {
    return {
        jsonClass: "TextValue",
        value: faker.random.word()
    }
};

export const generateLabel = (): Label => {
    return {
        name: faker.random.uuid(),
        value: generateValue()
    }
};

export const generateLabels = (count = faker.random.number({min: 1, max: 10})): Label[] => {
    return Array.apply(null, Array(count)).map(() => generateLabel());
}

function getRandomHealth(): Health {
    const healthArray: Health[] = [Health.Green, Health.Red, Health.Yellow];
    return healthArray[faker.random.number({min: 0, max: 3})];
}