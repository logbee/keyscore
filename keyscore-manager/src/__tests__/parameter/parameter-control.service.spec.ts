import {
    ParameterDescriptorJsonClass,
    ResolvedParameterDescriptor
} from "../../app/models/parameters/ParameterDescriptor";
import {Parameter, ParameterJsonClass} from "../../app/models/parameters/Parameter";
import {generateParameter, generateResolvedParameterDescriptor} from "../fake-data/pipeline-fakes";
import {zip} from "../../app/util";
import {ParameterControlService} from "../../app/common/parameter/service/parameter-control.service";

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
                formControlCount = result.controls[parameter.ref.uuid].value === parameter.value ? formControlCount + 1 : formControlCount;
            });

            expect(formControlCount).toBe(parameterDescriptors.length);
        });

    });
});