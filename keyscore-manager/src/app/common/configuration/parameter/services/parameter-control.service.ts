import {Injectable} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {zip} from "../../../../util";
import {
    ParameterDescriptor,
    ParameterDescriptorJsonClass, ResolvedParameterDescriptor
} from "../../../../models/parameters/ParameterDescriptor";
import {Parameter} from "../../../../models/parameters/Parameter";

@Injectable()
export class ParameterControlService {

    public toFormGroup(parameterDescriptors: ResolvedParameterDescriptor[], parameters: Parameter[]) {
        const group: any = {};
        const zippedParameters = zip([parameters, parameterDescriptors]);
        zippedParameters.forEach(([parameter, parameterDescriptor]) => {

            switch (parameterDescriptor.jsonClass) {
                case ParameterDescriptorJsonClass.TextListParameterDescriptor:
                    parameter.value = parameter.value ? parameter.value : [];
                    break;
                case ParameterDescriptorJsonClass.BooleanParameterDescriptor:
                    parameter.value = parameter.value !== null ? parameter.value : true;
                    break;
                case ParameterDescriptorJsonClass.FieldListParameterDescriptor:
                    parameter.value = parameter.value ? parameter.value : {};
                    break;

            }

            group[parameterDescriptor.ref.uuid] =
                parameterDescriptor.mandatory &&
                parameterDescriptor.jsonClass !== ParameterDescriptorJsonClass.BooleanParameterDescriptor ?
                    new FormControl(parameter.value || "", Validators.required)
                    : new FormControl(parameter.value || "");
        });
        return new FormGroup(group);
    }
}
