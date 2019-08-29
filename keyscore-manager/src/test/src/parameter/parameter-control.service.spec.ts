import {
    ParameterDescriptorJsonClass,
    ResolvedParameterDescriptor
} from "@keyscore-manager-models";
import {Parameter, ParameterJsonClass} from "@keyscore-manager-models";
import {generateParameter, generateResolvedParameterDescriptor} from "@keyscore-manager-test-fixtures";
import {ParameterControlService} from "@keyscore-manager-pipeline-parameters";
import * as _ from 'lodash'
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

    let parametersMap:Map<Parameter,ResolvedParameterDescriptor> = new Map(_.zip(parameters, parameterDescriptors));

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