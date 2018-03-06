import {Injectable} from "@angular/core";
import {ListParameter, Parameter} from "../streams/streams.model";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {createElementRangeValidator} from "../streams/stream-editor/filter-editor/parameter-list/parameter-list.component";

@Injectable()
export class ParameterControlService {
    constructor() {
    }

    toFormGroup(parameters: Parameter[]) {
        let group: any = {};
        parameters.forEach(parameter => {
            let formControl: FormControl;
            if (parameter.kind === 'list[string]') {

                formControl = new FormControl(parameter.value || '');
            }
            else {
                formControl = parameter.mandatory ? new FormControl(parameter.value || '', Validators.required)
                    : new FormControl(parameter.value || '');
            }

            group[parameter.name] = formControl;
        });
        return new FormGroup(group);
    }
}