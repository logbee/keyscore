import {ParameterComponent} from "../ParameterComponent";
import {Component} from "@angular/core";
import {DecimalParameterDescriptor, DecimalParameter} from "@keyscore-manager-models/src/main/parameters/decimal-parameter.model";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";

@Component({
    selector: ``,
    template: `
        <mat-form-field>
            <input #numberInput matInput type="number" [min]="descriptor.range?.start" [max]="descriptor.range?.end"
                   [step]="descriptor.range?.step" [placeholder]="descriptor.defaultValue"
                   [value]="parameter.value.toFixed(descriptor.decimals)"
                   (change)="onChange($event.target.value)">
            <mat-label>{{descriptor.displayName}}</mat-label>
            <button mat-button *ngIf="numberInput.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="numberInput.value='';onChange('')">
                <mat-icon>close</mat-icon>
            </button>
        </mat-form-field>
        <p class="parameter-warn" *ngIf="descriptor.mandatory && !numberInput.value">{{descriptor.displayName}} is
            required!</p>
        <p class="parameter-warn"
           *ngIf="descriptor.range && (numberInput.value > descriptor.range.end || numberInput.value < descriptor.range.start)">
            {{descriptor.displayName}} hast to be between {{descriptor.range.start}} and {{descriptor.range.end}}.
        </p>
        <p class="parameter-warn"
           *ngIf="descriptor.range && !validateStep(numberInput.value)">
            {{descriptor.displayName}} only allows inputs in {{descriptor.range.step}} - steps. Starting with
            {{descriptor.range.start}}.
        </p>
    `
})
export class DecimalParameterComponent extends ParameterComponent <DecimalParameterDescriptor, DecimalParameter> {
    private ref: ParameterRef;

    protected onInit(): void {
        this.ref = this.descriptor.ref;
    }

    private onChange(value: string): void {
        let decimal = Number(value).toFixed(this.descriptor.decimals);
        const parameter = new DecimalParameter(this.ref, +decimal);
        console.log("changed: ", parameter);
        this.emit(parameter)
    }

    private validateStep(value: string) {
        const val = +Number(value).toFixed(this.descriptor.decimals);
        const decimalFactor = Math.pow(10, this.descriptor.decimals);
        const powedVal = Math.round(val * decimalFactor);
        const powedStep = Math.round(this.descriptor.range.step * decimalFactor);

        return powedVal % powedStep === 0;
    }

}