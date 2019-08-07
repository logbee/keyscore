import {Injectable} from "@angular/core";
import {
    Parameter,
    ParameterGroupDescriptor,
    ParameterJsonClass,
    ResolvedParameterDescriptor
} from "keyscore-manager-models";
import {ParameterDescriptor, Parameter as newParameter,} from "../parameters/parameter.model";

@Injectable({
    providedIn: 'root'
})
export class ParameterFactoryService {

    private factories: Map<string, (descriptor: ParameterDescriptor,value?:any) => newParameter> = new Map();

    public newParameterDescriptorToParameter(parameterDescriptor: ParameterDescriptor,value?:any): newParameter {
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

    public parameterDescriptorToParameter(parameterDescriptor: ResolvedParameterDescriptor): Parameter {


        let type = parameterDescriptor.jsonClass.toString();
        type = type.substr(type.lastIndexOf('.') + 1);
        type = type.substr(0, type.length - "Descriptor".length);

        if (type === ParameterJsonClass.FieldNamePatternParameter) {
            return {
                ref: parameterDescriptor.ref,
                value: null,
                patternType: 0,
                jsonClass: ParameterJsonClass.FieldNamePatternParameter
            };
        }
        if (type === ParameterJsonClass.ParameterGroup) {
            const parameterDescriptors = (parameterDescriptor as ParameterGroupDescriptor).parameters;
            const parameters = parameterDescriptors.map(parameterDescriptor =>
                this.parameterDescriptorToParameter(parameterDescriptor));
            console.log("Parameters in Group: ", parameters);
            return {
                ref: parameterDescriptor.ref,
                jsonClass: ParameterJsonClass.ParameterGroup,
                parameters: {
                    jsonClass: ParameterJsonClass.ParameterSet,
                    parameters: parameters
                }
            }
        }

        return {ref: parameterDescriptor.ref, value: null, jsonClass: ParameterJsonClass[type]};

    }

    public register(jsonClass: string, f: (descriptor: ParameterDescriptor) => newParameter) {
        console.log("Resgister Parameter Factory for : ", jsonClass);
        this.factories.set(jsonClass, f);
    }
}