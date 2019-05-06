import * as faker from 'faker/locale/en_US';


export interface Ref{
    uuid:string;
}

export interface ParameterRef{
    id:string;
}

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
        id: faker.random.uuid()
    }
};
export const generateParameterRefs = (count = faker.random.number({min: 1, max: 10})): ParameterRef[] => {
    return Array.apply(null, Array(count)).map(() => generateParameterRef());
};