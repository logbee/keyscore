import {Injectable} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {zip} from "../../../util";
import {ParameterDescriptor} from "../../../models/pipeline-model/parameters/ParameterDescriptor";
import {Parameter} from "../../../models/pipeline-model/parameters/Parameter";

@Injectable()
export class ParameterControlService {

    public toFormGroup(parameterDescriptors: ParameterDescriptor[], parameters: Parameter[]) {
        const group: any = {};
        const zippedParameters = zip([parameteparamrs, parameterDescriptors]);
        zippedParameters.forEach(([parameter, parameterDescriptor]) => {

            switch (parameterDescriptor.jsonClass) {
                case "ListParameterDescriptor":
                    parameter.value = parameter.value ? parameter.value : [];
                    break;
                case "BooleanParameterDescriptor":
                    parameter.value = parameter.value !== null ? parameter.value : true;
                    break;
                case "MapParameterDescriptor":
                    parameter.value = parameter.value ? parameter.value : {};
                    break;

            }

            group[parameterDescriptor.name] =
                parameterDescriptor.mandatory &&
                parameterDescriptor.jsonClass !== "BooleanParameterDescriptor" ?
                    new FormControl(parameter.value || "", Validators.required)
                    : new FormControl(parameter.value || "");
        });
        return new FormGroup(group);
    }
}
