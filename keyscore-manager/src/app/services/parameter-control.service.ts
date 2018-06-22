import {Injectable} from "@angular/core";
import {Parameter, ParameterDescriptor} from "../pipelines/pipelines.model";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {zip} from "../util";

@Injectable()
export class ParameterControlService {
    constructor() {
    }

    toFormGroup(parameterDescriptors: ParameterDescriptor[],parameters:Parameter[]) {
        let group: any = {};
        let zippedParameters = zip([parameters,parameterDescriptors]);
        zippedParameters.forEach(([parameter,parameterDescriptor]) => {

            switch (parameterDescriptor.kind) {
                case 'list':
                    parameter.value = parameter.value ? parameter.value : [];
                    break;
                case 'boolean':
                    parameter.value = parameter.value ? parameter.value : true;
                    break;
                case 'map':
                    parameter.value = parameter.value ? parameter.value : {};
                    break;

            }

            group[parameterDescriptor.name] = parameterDescriptor.mandatory && parameterDescriptor.kind != 'boolean' ? new FormControl(parameter.value || '', Validators.required)
                : new FormControl(parameter.value || '');
        });
        return new FormGroup(group);
    }
}