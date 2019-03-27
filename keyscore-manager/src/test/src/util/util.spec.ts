import {generateResolvedParameterDescriptor} from "../../../../modules/keyscore-manager-models/src/test/fake-data/model-fakes";
import {ParameterDescriptorJsonClass} from "../../../../modules/keyscore-manager-models/src/main/parameters/ParameterDescriptor";
import {parameterDescriptorToParameter} from "../../../app/util";
import {Parameter, ParameterJsonClass} from "../../../../modules/keyscore-manager-models/src/main/parameters/Parameter";

describe('parameterDescriptorToParameter',()=>{
    it('should return a valid parameter',() => {
        const parameterDescriptor = generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.BooleanParameterDescriptor);

        let result:Parameter = parameterDescriptorToParameter(parameterDescriptor);

        expect(result.jsonClass).toBe(ParameterJsonClass.BooleanParameter);
    });
});