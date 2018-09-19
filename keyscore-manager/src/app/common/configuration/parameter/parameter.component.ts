import {Component, Input} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {
    ResolvedParameterDescriptor,
    ParameterDescriptorJsonClass
} from "../../../models/parameters/ParameterDescriptor";
import {Parameter} from "../../../models/parameters/Parameter";
import "./style/parameter-module-style.scss"

@Component({
    selector: "app-parameter",
    template: `
        <div [formGroup]="form">
            <div [ngSwitch]="parameterDescriptor.jsonClass">  
                <mat-form-field  *ngSwitchCase="jsonClass.TextParameterDescriptor">
                    <input matInput type="text" [placeholder]="parameterDescriptor.info.displayName"
                           [formControlName]="parameterDescriptor.ref.uuid"
                           [id]="parameterDescriptor.ref.uuid" [type]="'text'" [value]="parameter.value">
                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>

                <mat-form-field *ngSwitchCase="jsonClass.NumberParameterDescriptor">
                    <input matInput type="number" [value]="parameter.value" [placeholder]="parameterDescriptor.info.displayName"
                           [formControlName]="parameterDescriptor.ref.uuid"
                           [id]="parameterDescriptor.ref.uuid" [type]="'number'">
                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>
                         
                <parameter-list *ngSwitchCase="jsonClass.TextListParameterDescriptor" [formControlName]="parameterDescriptor.ref.uuid"
                                [id]="parameterDescriptor.ref.uuid" [parameter]="parameter"></parameter-list>
                <parameter-map *ngSwitchCase="jsonClass.FieldListParameterDescriptor" [formControlName]="parameterDescriptor.ref.uuid"
                               [id]="parameterDescriptor.ref.uuid"></parameter-map>

                <div *ngSwitchCase="jsonClass.BooleanParameterDescriptor"
                     class="toggleCheckbox" [id]="parameterDescriptor.ref.uuid">
                    <mat-slide-toggle [checked]="parameter.value" id="checkbox{{parameterDescriptor.ref.uuid}}"  
                                  [formControlName]="parameterDescriptor.name">
                        {{parameterDescriptor.info.displayName}} Value: {{parameter.value}}
                    </mat-slide-toggle>
                </div>
                <div  class="parameter-required" *ngIf="!isValid">{{parameterDescriptor.info.displayName}}
                    {{'PARAMETERCOMPONENT.ISREQUIRED' | translate}}
                </div>
            </div>
        </div>

    `,
    providers:[

    ]
})
export class ParameterComponent {
    @Input() public parameterDescriptor: ResolvedParameterDescriptor;
    @Input() public parameter: Parameter;
    @Input() public form: FormGroup;

    public jsonClass: typeof  ParameterDescriptorJsonClass = ParameterDescriptorJsonClass;

    get isValid() {
        return this.form.controls[this.parameterDescriptor.ref.uuid].valid;
    }
}
