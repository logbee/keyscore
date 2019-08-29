import {
    BooleanParameterDescriptor,
    Parameter,
    ParameterDescriptorJsonClass,
    ResolvedParameterDescriptor,
    SingleResolvedParameterDescriptor,
    ParameterGroupDescriptor,
    ParameterGroup
} from "@keyscore-manager-models";
import {Injectable} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import * as _ from "lodash";

@Injectable({
    providedIn: 'root'
})
export class ParameterControlService {

    public toFormGroup(parameterMapping: Map<Parameter, ResolvedParameterDescriptor>, directiveInstance: string = null) {
        console.log("ControlService Parameters::::::", parameterMapping.keys());
        let group: any = {};

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
                case ParameterDescriptorJsonClass.ParameterGroupDescriptor:
                    const parameterGroupMapping: Map<Parameter, ResolvedParameterDescriptor> =
                        new Map(_.zip((parameter as ParameterGroup).parameters.parameters, (parameterDescriptor as ParameterGroupDescriptor).parameters));
                    console.log("ParameterGroupMapping: ", parameterGroupMapping);
                    let parameterGroup: FormGroup = this.toFormGroup(parameterGroupMapping);
                    group = {
                        ...group,
                        ...parameterGroup.value
                    };
                    console.log("Group after ParameterGroup in ControlService: ", group);
                    break;
                default:
                    const descriptor = parameterDescriptor as SingleResolvedParameterDescriptor;
                    parameter.value = parameter.value || "";
                    group[id] = new FormControl(parameter.value, descriptor.mandatory ? Validators.required : null);

            }

        });
        return new FormGroup(group);
    }

}