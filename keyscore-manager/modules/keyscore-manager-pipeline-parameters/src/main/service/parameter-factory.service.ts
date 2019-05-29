import {Injectable} from "@angular/core";
import {Parameter, ParameterJsonClass, ResolvedParameterDescriptor} from "keyscore-manager-models";

@Injectable()
export class ParameterFactoryService {

    public parameterDescriptorToParameter(parameterDescriptor: ResolvedParameterDescriptor): Parameter {
        let type = parameterDescriptor.jsonClass.toString();
        type = type.substr(type.lastIndexOf('.') + 1);
        type = type.substr(0, type.length - "Descriptor".length);

        if (ParameterJsonClass.FieldNamePatternParameter === type) {
            return {
                ref: parameterDescriptor.ref,
                value: null,
                patternType: 0,
                jsonClass: ParameterJsonClass.FieldNamePatternParameter
            };
        }

        return {ref: parameterDescriptor.ref, value: null, jsonClass: ParameterJsonClass[type]};
    }
}