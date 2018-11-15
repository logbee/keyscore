import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {
    ResolvedParameterDescriptor,
    ParameterDescriptorJsonClass,
} from "../../models/parameters/ParameterDescriptor";
import {Parameter} from "../../models/parameters/Parameter";
import "./style/parameter-module-style.scss"

@Component({
    selector: "app-parameter",
    template: `
        <div [ngSwitch]="parameterDescriptor.jsonClass" [formGroup]="form" class="parameter-wrapper">
            <mat-form-field *ngSwitchCase="jsonClass.TextParameterDescriptor">
                <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.id"
                       [id]="parameter.ref.id" (change)="onChange()">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>

                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>
            <mat-form-field *ngSwitchCase="jsonClass.NumberParameterDescriptor">
                <input matInput type="number"
                       [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.id"
                       [id]="parameter.ref.id" (change)="onChange()">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <mat-form-field *ngSwitchCase="jsonClass.DecimalParameterDescriptor">
                <input matInput type="number"
                       [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.id"
                       [id]="parameter.ref.id" (change)="onChange()">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>


            <div *ngSwitchCase="jsonClass.BooleanParameterDescriptor"
                 class="toggleCheckbox" [id]="parameter.ref.id">
                <mat-slide-toggle [checked]="parameter.value" id="checkbox{{parameter.ref.id}}"
                                  [formControlName]="parameter.ref.id" (change)="onChange()">
                    {{parameterDescriptor.info.displayName}}
                </mat-slide-toggle>
            </div>

            <mat-form-field *ngSwitchCase="jsonClass.ChoiceParameterDescriptor" [id]="parameter.ref.id">
                <mat-select [formControlName]="parameter.ref.id"
                            [placeholder]="parameterDescriptor.defaultValue"
                            [attr.multiple]="parameterDescriptor.max > 1 ? '' :null" (change)="onChange()">
                    <mat-option *ngFor="let choice of parameterDescriptor.choices" [value]="choice.name"
                                [matTooltip]="choice.description" matTooltipPosition="before">{{choice.displayName}}
                    </mat-option>
                </mat-select>
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
            </mat-form-field>

            <mat-form-field *ngSwitchCase="jsonClass.ExpressionParameterDescriptor"
                            [id]="parameter.ref.id">
                <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.id"
                       [id]="parameter.ref.id" (change)="onChange()">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <mat-form-field *ngSwitchCase="jsonClass.FieldNameParameterDescriptor"
                            [id]="parameter.ref.id">
                <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.id"
                       [id]="parameter.ref.id" (change)="onChange()">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <mat-form-field *ngSwitchCase="jsonClass.FieldParameterDescriptor"
                            [id]="parameter.ref.id">
                <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="parameter.ref.id"
                       [id]="parameter.ref.id" (change)="onChange()">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <field-parameter-list *ngSwitchCase="jsonClass.FieldNameListParameterDescriptor"
                                  [formControlName]="parameter.ref.id"
                                  [id]="parameter.ref.id" [parameter]="parameter" ,
                                  [descriptor]="parameterDescriptor"
                                  (change)="onChange()"></field-parameter-list>

            <field-parameter-list *ngSwitchCase="jsonClass.TextListParameterDescriptor"
                                  [formControlName]="parameter.ref.id"
                                  [id]="parameter.ref.id" [parameter]="parameter"
                                  (change)="onChange()"></field-parameter-list>

            <parameter-map *ngSwitchCase="jsonClass.FieldListParameterDescriptor"
                           [formControlName]="parameter.ref.id"
                           [id]="parameter.ref.id" [parameter]="parameter"
                           (change)="onChange()"></parameter-map>


            <div class="parameter-required" *ngIf="!isValid">{{parameterDescriptor.info.displayName}}
                {{'PARAMETERCOMPONENT.ISREQUIRED' | translate}}
            </div>
        </div>
    `,
    providers: []
})
export class ParameterComponent {
    @Input() public parameterDescriptor: ResolvedParameterDescriptor;
    @Input() public parameter: Parameter;
    @Input() public form: FormGroup;
    @Output() public change: EventEmitter<void> = new EventEmitter();

    public jsonClass: typeof ParameterDescriptorJsonClass = ParameterDescriptorJsonClass;

    onChange() {
        this.change.emit();
    }

    get isValid() {
        return this.form.controls[this.parameter.ref.id].valid;
    }
}
