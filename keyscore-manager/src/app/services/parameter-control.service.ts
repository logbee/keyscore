import {Injectable} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {Parameter, ParameterDescriptor} from "../pipelines/pipelines.model";
import {zip} from "../util";

@Injectable()
export class ParameterControlService {

    public toFormGroup(parameterDescriptors: ParameterDescriptor[], parameters: Parameter[]) {
        const group: any = {};
        const zippedParameters = zip([parameters, parameterDescriptors]);
        zippedParameters.forEach(([parameter, parameterDescriptor]) => {

            switch (parameterDescriptor.kind) {
                case "list":
                    parameter.value = parameter.value ? parameter.value : [];
                    break;
                case "boolean":
                    parameter.value = parameter.value ? parameter.value : true;
                    break;
                case "map":
                    parameter.value = parameter.value ? parameter.value : {};
                    break;

            }

            group[parameterDescriptor.name] =
                parameterDescriptor.mandatory &&
                parameterDescriptor.kind !== "boolean" ? new FormControl(parameter.value || "", Validators.required)
                    : new FormControl(parameter.value || "");
        });
        return new FormGroup(group);
    }
}
