import {Component, Input} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {ParameterDescriptor} from "../../models/pipeline-model/parameters/ParameterDescriptor";
import {Parameter} from "../../models/pipeline-model/parameters/Parameter";
import "./style/parameter-module-style.scss"

@Component({
    selector: "app-parameter",
    template: `
        <div [formGroup]="form">
            <div [ngSwitch]="parameterDescriptor.jsonClass">    
                <mat-form-field  *ngSwitchCase="'TextParameterDescriptor'">
                    <input matInput type="text" [placeholder]="parameterDescriptor.displayName" [formControlName]="parameterDescriptor.name"
                           [id]="parameterDescriptor.name" [type]="'text'" [value]="parameter.value">
                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>

                <mat-form-field *ngSwitchCase="'IntParameterDescriptor'">
                    <input matInput type="number" [value]="parameter.value" [placeholder]="parameterDescriptor.displayName" [formControlName]="parameterDescriptor.name"
                           [id]="parameterDescriptor.name" [type]="'number'">
                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>
                         
                <parameter-list *ngSwitchCase="'ListParameterDescriptor'" [formControlName]="parameterDescriptor.name"
                                [id]="parameterDescriptor.name" [parameter]="parameter"></parameter-list>
                <parameter-map *ngSwitchCase="'MapParameterDescriptor'" [formControlName]="parameterDescriptor.name"
                               [id]="parameterDescriptor.name"></parameter-map>

                <div *ngSwitchCase="'BooleanParameterDescriptor'"
                     class="toggleCheckbox" [id]="parameterDescriptor.name">
                    <mat-slide-toggle [checked]="parameter.value" id="checkbox{{parameterDescriptor.name}}"  
                                  [formControlName]="parameterDescriptor.name">
                        {{parameterDescriptor.displayName}} Value: {{parameter.value}}
                    </mat-slide-toggle>
                </div>
                <div  class="parameter-required" *ngIf="!isValid">{{parameterDescriptor.displayName}}
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
