import {Component, Input} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {ParameterDescriptor} from "../../models/pipeline-model/parameters/ParameterDescriptor";
import {Parameter} from "../../models/pipeline-model/parameters/Parameter";

@Component({
    selector: "app-parameter",
    template: `
        <div [formGroup]="form">
            <label class="mat-subheading-1 font-weight-bold" [attr.for]="parameterDescriptor.name">{{parameterDescriptor.displayName}}</label>
            <div [ngSwitch]="parameterDescriptor.jsonClass">
                <mat-form-field *ngSwitchCase="'TextParameterDescriptor'">
                    <input matInput type="text" placeholder="Field name" [formControlName]="parameterDescriptor.name"
                           [id]="parameterDescriptor.name" [type]="'text'" [(ngModel)]="value">
                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>

                <mat-form-field *ngSwitchCase="'IntParameterDescriptor'">
                    <input matInput type="number" placeholder="Value" [formControlName]="parameterDescriptor.name"
                           [id]="parameterDescriptor.name" [type]="'number'" [(ngModel)]="value">
                    <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                        <mat-icon>close</mat-icon>
                    </button>
                </mat-form-field>
                         
                <parameter-list *ngSwitchCase="'ListParameterDescriptor'" [formControlName]="parameterDescriptor.name"
                                [id]="parameterDescriptor.name"></parameter-list>
                <parameter-map *ngSwitchCase="'MapParameterDescriptor'" [formControlName]="parameterDescriptor.name"
                               [id]="parameterDescriptor.name"></parameter-map>

                <div *ngSwitchCase="'BooleanParameterDescriptor'"
                     class="toggleCheckbox" [id]="parameterDescriptor.name">
                    <mat-checkbox [(ngModel)]="checked" id="checkbox{{parameterDescriptor.name}}" 
                                  [formControlName]="parameterDescriptor.name">
                    </mat-checkbox>
                </div>
                <div *ngIf="!isValid">{{parameterDescriptor.displayName}}
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
