import {ParameterComponent} from "../ParameterComponent";
import {Component} from "@angular/core";
import {NumberParameterDescriptor, NumberParameter} from "@/../modules/keyscore-manager-models/src/main/parameters/number-parameter.model";
import {ParameterRef} from "@/../modules/keyscore-manager-models/src/main/common/Ref";

@Component({
    selector: `parameter-number`,
    template: `
        <div *ngIf="descriptor.range;then range else noRange"></div>

        <ng-template #noRange>
            <mat-form-field>
                <input #numberInput matInput type="number" [placeholder]="descriptor.defaultValue"
                       [value]="parameter.value"
                       (change)="onChange($event.target.value)">
                <mat-label>{{descriptor.displayName}}</mat-label>
                <button mat-button *ngIf="numberInput.value" matSuffix mat-icon-button aria-label="Clear"
                        (click)="numberInput.value='';onChange('')">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>
            <p class="parameter-warn" *ngIf="descriptor.mandatory && !numberInput.value" translate 
               [translateParams]="{name:descriptor.displayName}">
                PARAMETER.IS_REQUIRED
            </p>
            <p class="parameter-warn"
               *ngIf="!templateNumber.isInteger(+numberInput.value)" translate [translateParams]="{name:descriptor.displayName}">
                PARAMETER.INTEGER_WARNING
            </p>
        </ng-template>

        <ng-template #range>
            <ks-slider [label]="descriptor.displayName" [range]="descriptor.range"
                       [value]="parameter.value" (changed)="onChange($event)"></ks-slider>
        </ng-template>

    `,
    styleUrls:['../../style/parameter-module-style.scss']

})
export class NumberParameterComponent extends ParameterComponent<NumberParameterDescriptor, NumberParameter> {
    private ref: ParameterRef;

    private templateNumber = Number;

    protected onInit(): void {
        this.ref = this.descriptor.ref;
    }

    private onChange(value: number): void {
        const parameter = new NumberParameter(this.ref, Math.round(value));
        console.log("changed: ", parameter);
        this.emit(parameter)
    }
}