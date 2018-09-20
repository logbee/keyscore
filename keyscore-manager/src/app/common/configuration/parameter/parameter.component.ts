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
            <div [ngSwitch]="parameterDescriptor.jsonClass">
                <mat-form-field *ngSwitchCase="jsonClass.TextParameterDescriptor">
                    <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                           [formControlName]="parameterDescriptor.ref.uuid"
                           [id]="parameterDescriptor.ref.uuid" [value]="parameter.value">
                    <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>

                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>

                <mat-form-field *ngSwitchCase="jsonClass.NumberParameterDescriptor">
                    <input matInput type="number" [value]="parameter.value"
                           [placeholder]="parameterDescriptor.defaultValue"
                           [formControlName]="parameterDescriptor.ref.uuid"
                           [id]="parameterDescriptor.ref.uuid">
                    <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>

                <mat-form-field *ngSwitchCase="jsonClass.DecimalParameterDescriptor">
                    <input matInput type="number" [value]="parameter.value"
                           [placeholder]="parameterDescriptor.defaultValue"
                           [formControlName]="parameterDescriptor.ref.uuid"
                           [id]="parameterDescriptor.ref.uuid">
                    <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>

                <parameter-list *ngSwitchCase="jsonClass.TextListParameterDescriptor"
                                [formControlName]="parameterDescriptor.ref.uuid"
                                [id]="parameterDescriptor.ref.uuid" [parameter]="parameter"></parameter-list>
                <parameter-map *ngSwitchCase="jsonClass.FieldListParameterDescriptor"
                               [formControlName]="parameterDescriptor.ref.uuid"
                               [id]="parameterDescriptor.ref.uuid"></parameter-map>

                <div *ngSwitchCase="jsonClass.BooleanParameterDescriptor"
                     class="toggleCheckbox" [id]="parameterDescriptor.ref.uuid">
                    <mat-slide-toggle [checked]="parameter.value" id="checkbox{{parameterDescriptor.ref.uuid}}"
                                      [formControlName]="parameterDescriptor.ref.uuid">
                        {{parameterDescriptor.info.displayName}}
                    </mat-slide-toggle>
                </div>

                <div *ngSwitchCase="jsonClass.ChoiceParameterDescriptor" [id]="parameterDescriptor.ref.uuid">
                    <mat-select [formControlName]="parameterDescriptor.ref.uuid"
                                [placeholder]="parameterDescriptor.defaultValue" multiple>
                        <mat-option *ngFor="let choice of parameterDescriptor.choices" [value]="choice.displayName"
                                    [matTooltip]="choice.description" matTooltipPosition="before"></mat-option>
                    </mat-select>
                </div>

                <mat-form-field *ngSwitchCase="jsonClass.ExpressionParameterDescriptor"
                                [id]="parameterDescriptor.ref.uuid">
                    <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                           [formControlName]="parameterDescriptor.ref.uuid"
                           [id]="parameterDescriptor.ref.uuid" [value]="parameter.value">
                    <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>

                <mat-form-field *ngSwitchCase="jsonClass.FieldNameParameterDescriptor"
                                [id]="parameterDescriptor.ref.uuid">
                    <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                           [formControlName]="parameterDescriptor.ref.uuid"
                           [id]="parameterDescriptor.ref.uuid" [value]="parameter.value">
                    <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>

                <mat-form-field *ngSwitchCase="jsonClass.FieldParameterDescriptor"
                                [id]="parameterDescriptor.ref.uuid">
                    <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                           [formControlName]="parameterDescriptor.ref.uuid"
                           [id]="parameterDescriptor.ref.uuid" [value]="parameter.value">
                    <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>

                <parameter-list *ngSwitchCase="jsonClass.FieldNameListParameterDescriptor"
                                [formControlName]="parameterDescriptor.ref.uuid"
                                [id]="parameterDescriptor.ref.uuid" [parameter]="parameter"></parameter-list>

                <!--<div class="parameter-required" *ngIf="!isValid">{{parameterDescriptor.info.displayName}}
                    {{'PARAMETERCOMPONENT.ISREQUIRED' | translate}}
                </div>-->
            </div>

    `,
    providers: []
})
export class ParameterComponent {
    @Input() public parameterDescriptor: ResolvedParameterDescriptor;
    @Input() public parameter: Parameter;
    @Input() public form: FormGroup;

    public jsonClass: typeof ParameterDescriptorJsonClass = ParameterDescriptorJsonClass;

    /*get isValid() {
        return this.form.controls[this.parameterDescriptor.ref.uuid].valid;
    }*/
}
