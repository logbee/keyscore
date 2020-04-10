import * as uuid from 'uuid';


export interface Ref{
    uuid:string;
}

export interface ParameterRef{
    id:string;
}

export const generateRef = (): Ref => {
    return {
        uuid: uuid()
    }
};

export const generateParameterRef = (): ParameterRef => {
    return {
        id: uuid()
    }
};
