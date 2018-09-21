import * as faker from 'faker/locale/en_US';
import {Connection} from "./connection.model";
import {ResolvedParameterDescriptor} from "../../../../models/parameters/ParameterDescriptor";
import {
    generateResolvedParameterDescriptors,
    generateWordList
} from "../../../../../__tests__/fake-data/pipeline-fakes";

export interface BlockDescriptor {
    ref: string;
    displayName: string;
    description: string;
    previousConnection: Connection;
    nextConnection: Connection;
    parameters: ResolvedParameterDescriptor[];
    categories: string[];
}

export const generateBlockDescriptor = (categories: string[] = null): BlockDescriptor => {
    const defaultCategories = ['Sink', 'Source', 'Filter'];
    const specialCategories = ['Elastic', 'Kafka', 'Amazon'];
    if (categories === null) {
        categories = [defaultCategories[faker.random.number({min: 1, max: defaultCategories.length - 1})],
            specialCategories[faker.random.number({min: 1, max: specialCategories.length - 1})]];
    }
    let previousPermitted = categories.includes('Sink') || categories.includes('Filter');
    let nextPermitted = categories.includes('Source') || categories.includes('Filter');
    if(!previousPermitted && !nextPermitted){
        nextPermitted = true;
    }
    return {
        ref: faker.random.uuid(),
        displayName: faker.random.word(),
        description: faker.lorem.sentence(),
        previousConnection: {
            connectableTypes: previousPermitted ? ['default-out'] : [],
            isPermitted: true,
            connectionType: previousPermitted ? 'default-in' : 'no-connection-in'
        },
        nextConnection: {
            connectableTypes: nextPermitted ? ['default-in'] : [],
            isPermitted: true,
            connectionType: nextPermitted ? 'default-out' : 'no-connection-out'
        },
        parameters: generateResolvedParameterDescriptors(),
        categories: categories
    };
};

export const generateBlockDescriptors = (count: number = faker.random.number({min: 1, max: 10})) => {
    return Array.apply(null, Array(count)).map(() => generateBlockDescriptor())
}