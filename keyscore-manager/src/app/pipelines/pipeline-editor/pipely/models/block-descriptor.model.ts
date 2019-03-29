import * as faker from 'faker/locale/en_US';
import {Connection} from "./connection.model";
import {ResolvedParameterDescriptor} from "../../../../../../modules/keyscore-manager-models/src/main/parameters/ParameterDescriptor";
import {
    generateResolvedParameterDescriptors,
    generateWordList
} from "../../../../../../modules/keyscore-manager-test-fixtures/src/main/data-generators";
import {Ref} from "../../../../../../modules/keyscore-manager-models/src/main/common/Ref";
import {Category, ResolvedCategory} from "../../../../../../modules/keyscore-manager-models/src/main/descriptors/Category";
import {Icon} from "../../../../../../modules/keyscore-manager-models/src/main/descriptors/Icon";

export interface BlockDescriptor {
    ref: Ref;
    displayName: string;
    description: string;
    previousConnection: Connection;
    nextConnection: Connection;
    parameters: ResolvedParameterDescriptor[];
    categories: ResolvedCategory[];
    icon?: Icon;
}


export const generateBlockDescriptor = (categories: ResolvedCategory[] = null): BlockDescriptor => {
    const defaultCategories = [
        {displayName: 'Sink', name: "sink"},
        {displayName: 'Source', name: 'source'},
        {displayName: 'Filter', name: "filter"}
    ];
    const specialCategories = [
        {displayName: 'Elastic', name: 'elastic'},
        {displayName: 'Kafka', name: 'kafka'},
        {displayName: 'Amazon', name: 'amazon'}
    ];
    if (categories === null) {
        categories = [defaultCategories[faker.random.number({min: 1, max: defaultCategories.length - 1})],
            specialCategories[faker.random.number({min: 1, max: specialCategories.length - 1})]];
    }
    let previousPermitted = categories.map(cat => cat.name).includes('sink') ||
        categories.map(cat => cat.name).includes('filter');
    let nextPermitted = categories.map(cat => cat.name).includes('source') ||
        categories.map(cat => cat.name).includes('filter');
    if (!previousPermitted && !nextPermitted) {
        nextPermitted = true;
    }
    return {
        ref: faker.random.uuid(),
        displayName: faker.random.word(),
        description: faker.lorem.sentence(),
        previousConnection: {
            connectableTypes: previousPermitted ? ['default-out'] : [],
            connectionType: previousPermitted ? 'default-in' : 'no-connection-in'
        },
        nextConnection: {
            connectableTypes: nextPermitted ? ['default-in'] : [],
            connectionType: nextPermitted ? 'default-out' : 'no-connection-out'
        },
        parameters: generateResolvedParameterDescriptors(),
        categories: categories
    };
};

export const generateBlockDescriptors = (count: number = faker.random.number({min: 1, max: 10})) => {
    return Array.apply(null, Array(count)).map(() => generateBlockDescriptor())
}