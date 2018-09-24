import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {
    ResolvedParameterDescriptor,
    ParameterDescriptorJsonClass, ChoiceParameterDescriptor
} from "../../models/parameters/ParameterDescriptor";
import {Parameter} from "../../models/parameters/Parameter";
import "./style/parameter-module-style.scss"

@Component({
    selector: "app-parameter",
    template: `
        <div [ngSwitch]="parameterDescriptor.jsonClass" [formGroup]="form" class="parameter-wrapper">
            <mat-form-field *ngSwitchCase="jsonClass.TextParameterDescriptor">
                <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.uuid"
                       [id]="parameter.ref.uuid">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>

                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <mat-form-field *ngSwitchCase="jsonClass.NumberParameterDescriptor">
                <input matInput type="number"
                       [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.uuid"
                       [id]="parameter.ref.uuid">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <mat-form-field *ngSwitchCase="jsonClass.DecimalParameterDescriptor">
                <input matInput type="number"
                       [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.uuid"
                       [id]="parameter.ref.uuid">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>


            <div *ngSwitchCase="jsonClass.BooleanParameterDescriptor"
                 class="toggleCheckbox" [id]="parameter.ref.uuid">
                <mat-slide-toggle [checked]="parameter.value" id="checkbox{{parameter.ref.uuid}}"
                                  [formControlName]="parameter.ref.uuid">
                    {{parameterDescriptor.info.displayName}}
                </mat-slide-toggle>
            </div>

            <mat-form-field *ngSwitchCase="jsonClass.ChoiceParameterDescriptor" [id]="parameter.ref.uuid">
                <mat-select [formControlName]="parameter.ref.uuid"
                            [placeholder]="parameterDescriptor.defaultValue"
                            [attr.multiple]="parameterDescriptor.max > 1 ? '' :null">
                    <mat-option *ngFor="let choice of parameterDescriptor.choices" [value]="choice.name"
                                [matTooltip]="choice.description" matTooltipPosition="before">{{choice.displayName}}</mat-option>
                </mat-select>
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
            </mat-form-field>

            <mat-form-field *ngSwitchCase="jsonClass.ExpressionParameterDescriptor"
                            [id]="parameter.ref.uuid">
                <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.uuid"
                       [id]="parameter.ref.uuid">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <mat-form-field *ngSwitchCase="jsonClass.FieldNameParameterDescriptor"
                            [id]="parameter.ref.uuid">
                <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.uuid"
                       [id]="parameter.ref.uuid">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <mat-form-field *ngSwitchCase="jsonClass.FieldParameterDescriptor"
                            [id]="parameter.ref.uuid">
                <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.uuid"
                       [id]="parameter.ref.uuid">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <parameter-list *ngSwitchCase="jsonClass.FieldNameListParameterDescriptor"
                            [formControlName]="parameter.ref.uuid"
                            [id]="parameter.ref.uuid" [parameter]="parameter"></parameter-list>

            <parameter-list *ngSwitchCase="jsonClass.TextListParameterDescriptor"
                            [formControlName]="parameter.ref.uuid"
                            [id]="parameter.ref.uuid" [parameter]="parameter"></parameter-list>

            <parameter-map *ngSwitchCase="jsonClass.FieldListParameterDescriptor"
                           [formControlName]="parameter.ref.uuid"
                           [id]="parameter.ref.uuid"></parameter-map>


            <div class="parameter-required" *ngIf="!isValid">{{parameterDescriptor.info.displayName}}
                {{'PARAMETERCOMPONENT.ISREQUIRED' | translate}}
            </div>
        </div>

    `,
    providers: []
})
export class ParameterComponent implements OnInit {
    @Input() public parameterDescriptor: ResolvedParameterDescriptor;
    @Input() public parameter: Parameter;
    @Input() public form: FormGroup;

    public jsonClass: typeof ParameterDescriptorJsonClass = ParameterDescriptorJsonClass;

    ngOnInit() {

    }

    get isValid() {
        return this.form.controls[this.parameter.ref.uuid].valid;
    }
}
