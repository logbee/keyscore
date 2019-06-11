import {Injectable} from "@angular/core";
import {
    Parameter,
    ParameterGroupDescriptor,
    ParameterJsonClass,
    ResolvedParameterDescriptor
} from "keyscore-manager-models";

@Injectable()
export class ParameterFactoryService {

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
}