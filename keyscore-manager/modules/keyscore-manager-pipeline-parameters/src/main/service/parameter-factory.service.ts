import {Injectable} from "@angular/core";
import {
    Parameter,
    ParameterGroupDescriptor,
    ParameterJsonClass,
    ResolvedParameterDescriptor
} from "keyscore-manager-models";

@Injectable({
    providedIn: 'root'
})
export class ParameterFactoryService {

    private factories: Map<string, (descriptor: ResolvedParameterDescriptor) => Parameter> = new Map();

    public parameterDescriptorToParameter(parameterDescriptor: ResolvedParameterDescriptor): Parameter {


        let type = parameterDescriptor.jsonClass.toString();
        type = type.substr(type.lastIndexOf('.') + 1);
        type = type.substr(0, type.length - "Descriptor".length);

        const factory = this.factories.get(parameterDescriptor.jsonClass);
        if (factory) {
            return factory(parameterDescriptor);
        } else {
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
    }

    public register(jsonClass: string, f: (descriptor: ResolvedParameterDescriptor) => Parameter) {
        console.log("Resgister: ", jsonClass);
        this.factories.set(jsonClass, f);
    }
}