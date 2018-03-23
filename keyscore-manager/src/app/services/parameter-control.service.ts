import {Injectable} from "@angular/core";
import {Parameter, ParameterDescriptor} from "../streams/streams.model";
import {FormControl, FormGroup, Validators} from "@angular/forms";

@Injectable()
export class ParameterControlService {
    constructor() {
    }

    toFormGroup(parameters: ParameterDescriptor[]) {
        let group: any = {};
        parameters.forEach(parameter => {

            switch (parameter.kind) {
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

            group[parameter.name] = parameter.mandatory && parameter.kind != 'boolean' ? new FormControl(parameter.value || '', Validators.required)
                : new FormControl(parameter.value || '');
        });
        return new FormGroup(group);
    }
}