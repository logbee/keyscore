import {
    BooleanParameterDescriptor,
    ParameterDescriptorJsonClass,
    ResolvedParameterDescriptor,
    SingleResolvedParameterDescriptor,
    Parameter
} from "keyscore-manager-models";
import {Injectable} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";

@Injectable()
export class ParameterControlService {

    public toFormGroup(parameterMapping: Map<Parameter, ResolvedParameterDescriptor>, directiveInstance?: string) {
        const group: any = {};
        parameterMapping.forEach((parameterDescriptor, parameter) => {
            const id = directiveInstance ? directiveInstance + ':' + parameter.ref.id : parameter.ref.id;
            switch (parameterDescriptor.jsonClass) {
                case ParameterDescriptorJsonClass.TextListParameterDescriptor:
                case ParameterDescriptorJsonClass.FieldNameListParameterDescriptor:
                case ParameterDescriptorJsonClass.FieldListParameterDescriptor:
                case ParameterDescriptorJsonClass.FieldDirectiveSequenceParameterDescriptor:
                    parameter.value = parameter.value ? parameter.value : [];
                    group[id] = new FormControl(parameter.value);
                    break;
                case ParameterDescriptorJsonClass.ChoiceParameterDescriptor:
                    group[id] = new FormControl(parameter.value);
                    break;
                case ParameterDescriptorJsonClass.BooleanParameterDescriptor:
                    parameter.value = parameter.value !== null ? parameter.value : true;
                    group[id] = new FormControl(parameter.value, (parameterDescriptor as BooleanParameterDescriptor).mandatory ? Validators.required : null);
                    break;
                case ParameterDescriptorJsonClass.NumberParameterDescriptor:
                case ParameterDescriptorJsonClass.DecimalParameterDescriptor:
                    parameter.value = parameter.value === 0 ? null : parameter.value;
                    group[id] = new FormControl(parameter.value, (parameterDescriptor as SingleResolvedParameterDescriptor).mandatory ? Validators.required : null);
                    break;
                default:
                    const descriptor = parameterDescriptor as SingleResolvedParameterDescriptor;
                    parameter.value = parameter.value ? parameter.value : "";
                    group[id] = new FormControl(parameter.value, descriptor.mandatory ? Validators.required : null);

            }

        });
        return new FormGroup(group);
    }
}