import {generateResolvedParameterDescriptor} from "../fake-data/pipeline-fakes";
import {ParameterDescriptorJsonClass} from "../../app/models/parameters/ParameterDescriptor";
import {parameterDescriptorToParameter} from "../../app/util";
import {Parameter, ParameterJsonClass} from "../../app/models/parameters/Parameter";

describe('parameterDescriptorToParameter',()=>{
    it('should return a valid parameter',() => {
        const parameterDescriptor = generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.BooleanParameterDescriptor);

        let result:Parameter = parameterDescriptorToParameter(parameterDescriptor);

        expect(result.jsonClass).toBe(ParameterJsonClass.BooleanParameter);
    });
});