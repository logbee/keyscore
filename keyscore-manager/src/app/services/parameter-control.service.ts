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
            group[parameter.name] = parameter.mandatory ? new FormControl(parameter.value || '', Validators.required)
                : new FormControl(parameter.value || '');
        });
        return new FormGroup(group);
    }
}