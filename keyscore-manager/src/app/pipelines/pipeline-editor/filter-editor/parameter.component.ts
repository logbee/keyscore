import {Component, Input} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {ParameterDescriptor} from "../../../models/pipeline-model/parameters/ParameterDescriptor";
import {Parameter} from "../../../models/pipeline-model/parameters/Parameter";

@Component({
    selector: "app-parameter",
    template: `
        <div [formGroup]="form">
            <label [attr.for]="parameterDescriptor.name">{{parameterDescriptor.displayName}}</label>
            <div [ngSwitch]="parameterDescriptor.kind">
                <input class="form-control" *ngSwitchCase="'text'" [formControlName]="parameterDescriptor.name"
                       [id]="parameterDescriptor.name" [type]="'text'">

                <input class="form-control" *ngSwitchCase="'int'" [formControlName]="parameterDescriptor.name"
                       [id]="parameterDescriptor.name" [type]="'number'">
                <parameter-list *ngSwitchCase="'list'" [formControlName]="parameterDescriptor.name"
                                [id]="parameterDescriptor.name"></parameter-list>
                <parameter-map *ngSwitchCase="'map'" [formControlName]="parameterDescriptor.name"
                               [id]="parameterDescriptor.name"></parameter-map>

                <div *ngSwitchCase="'boolean'" class="toggleCheckbox" [id]="parameterDescriptor.name">

                    <input type="checkbox" id="checkbox{{parameterDescriptor.name}}" class="ios-toggle"
                           [formControlName]="parameterDescriptor.name">
                    <label for="checkbox{{parameterDescriptor.name}}" class="checkbox-label" data-off=""
                           data-on=""></label>

                </div>

                <div class="text-danger" *ngIf="!isValid">{{parameterDescriptor.displayName}}
                    {{'PARAMETERCOMPONENT.ISREQUIRED' | translate}}
                </div>
            </div>
        </div>

    `
})
export class ParameterComponent {
    @Input() public parameterDescriptor: ParameterDescriptor;
    @Input() public parameter: Parameter;
    @Input() public form: FormGroup;

    get isValid() {
        return this.form.controls[this.parameterDescriptor.name].valid;
    }
}
