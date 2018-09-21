import * as faker from 'faker/locale/en_US';

export interface Connection{
    connectableTypes:string[];
    isPermitted:boolean;
    connectionType:string;
}
