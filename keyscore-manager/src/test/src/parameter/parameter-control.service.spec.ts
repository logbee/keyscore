import {
    ParameterDescriptorJsonClass,
    ResolvedParameterDescriptor
} from "../../../../modules/keyscore-manager-models/src/main/parameters/ParameterDescriptor";
import {Parameter, ParameterJsonClass} from "../../../../modules/keyscore-manager-models/src/main/parameters/Parameter";
import {generateParameter, generateResolvedParameterDescriptor} from "../../../../modules/keyscore-manager-models/src/test/fake-data/model-fakes";
import {zip} from "../../../app/util";
import {ParameterControlService} from "../../../../modules/keyscore-manager-pipeline-parameters/src/main/service/parameter-control.service";

describe('Service: ParameterControlService',()=> {
    let service:ParameterControlService = new ParameterControlService();

    const descriptorJsonClasses: ParameterDescriptorJsonClass[] = [ParameterDescriptorJsonClass.ExpressionParameterDescriptor,
        ParameterDescriptorJsonClass.ChoiceParameterDescriptor, ParameterDescriptorJsonClass.BooleanParameterDescriptor,
        ParameterDescriptorJsonClass.DecimalParameterDescriptor, ParameterDescriptorJsonClass.FieldListParameterDescriptor,
        ParameterDescriptorJsonClass.FieldNameListParameterDescriptor, ParameterDescriptorJsonClass.FieldNameParameterDescriptor,
        ParameterDescriptorJsonClass.FieldParameterDescriptor, ParameterDescriptorJsonClass.NumberParameterDescriptor,
        ParameterDescriptorJsonClass.TextListParameterDescriptor, ParameterDescriptorJsonClass.TextParameterDescriptor];

    const parameterJsonClasses = [ParameterJsonClass.ExpressionParameter, ParameterJsonClass.ChoiceParameter,
        ParameterJsonClass.BooleanParameter, ParameterJsonClass.DecimalParameter, ParameterJsonClass.FieldListParameter,
        ParameterJsonClass.FieldNameListParameter, ParameterJsonClass.FieldNameParameter, ParameterJsonClass.FieldParameter,
        ParameterJsonClass.NumberParameter, ParameterJsonClass.TextListParameter, ParameterJsonClass.TextParameter];

    let parameterDescriptors: ResolvedParameterDescriptor[] = descriptorJsonClasses.map(jsonClass => generateResolvedParameterDescriptor(jsonClass));
    let parameters: Parameter[] = parameterJsonClasses.map(jsonClass => generateParameter(jsonClass));

    let parametersMap:Map<Parameter,ResolvedParameterDescriptor> = new Map(zip([parameters, parameterDescriptors]));

    describe('toFormGroup', () => {
        it('should create the form group with formControls for each descriptor named by them and fill them with the parameter values', () => {
            let result = service.toFormGroup(parametersMap);

            let formControlCount = 0;
            parametersMap.forEach((descriptor,parameter) => {
                formControlCount = result.controls[parameter.ref.id].value === parameter.value ? formControlCount + 1 : formControlCount;
            });
            console.log(result.controls);
            expect(formControlCount).toBe(parameterDescriptors.length);
        });

    });
});