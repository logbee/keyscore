import * as faker from 'faker/locale/en_US';
import {PipelineInstance} from "../../app/models/pipeline-model/PipelineInstance";
import {Health} from "../../app/models/common/Health";

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

function getRandomHealth(): Health {
    const healthArray: Health[] = [Health.Green, Health.Red, Health.Yellow];
    return healthArray[faker.random.number({min: 0, max: 3})];
}