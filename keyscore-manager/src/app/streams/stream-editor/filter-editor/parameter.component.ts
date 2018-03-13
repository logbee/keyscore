import {Component, Input} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {Parameter, ParameterDescriptor} from "../../streams.model";


@Component({
    selector: 'app-parameter',
    template: `
        <div [formGroup]="form">
            <label [attr.for]="parameter.name">{{parameter.displayName}}</label>
            <div [ngSwitch]="parameter.kind">
                <input class="form-control" *ngSwitchCase="'text'" [formControlName]="parameter.name"
                       [id]="parameter.name" [type]="'text'">

                <input class="form-control" *ngSwitchCase="'int'" [formControlName]="parameter.name"
                       [id]="parameter.name" [type]="'number'">
                <parameter-list *ngSwitchCase="'list'" [formControlName]="parameter.name"
                                [id]="parameter.name" [parameter]="parameter"></parameter-list>
            </div>
            <div class="text-danger" *ngIf="!isValid">{{parameter.displayName}} is required</div>
        </div>
    `
})
export class ParameterComponent {
    @Input() parameter: ParameterDescriptor;
    @Input() form: FormGroup;

    get isValid() {
        return this.form.controls[this.parameter.name].valid;
    }
}