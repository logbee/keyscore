import {Injectable} from "@angular/core";
import {
    ParameterDescriptor,
    Parameter,
    ListParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter.model";

@Injectable({
    providedIn: 'root'
})
export class ParameterFactoryService {

    private factories: Map<string, (descriptor: ParameterDescriptor,value?:any) => Parameter> = new Map();

    public parameterDescriptorToParameter(parameterDescriptor: ParameterDescriptor, value?:any): Parameter | ListParameter  {
        const factory = this.factories.get(parameterDescriptor.jsonClass);
        if (factory) {
            if(value){
                return factory(parameterDescriptor,value);
            }
            return factory(parameterDescriptor);
        }
        throw Error(`No factory found for ${parameterDescriptor.jsonClass}.
                              Maybe you forgot to register the Parameter at the ParameterFactoryService?`)
    }

    public register(jsonClass: string, f: (descriptor: ParameterDescriptor) => Parameter | ListParameter) {
        console.log("Resgister Parameter Factory for : ", jsonClass);
        this.factories.set(jsonClass, f);
    }
}
