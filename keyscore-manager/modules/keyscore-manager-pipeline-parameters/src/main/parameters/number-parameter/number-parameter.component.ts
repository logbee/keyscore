import {ParameterComponent} from "../ParameterComponent";
import {Component} from "@angular/core";
import {ParameterRef} from "keyscore-manager-models";
import {NumberParameter, NumberParameterDescriptor} from "./number-parameter.model";

@Component({
    selector: `parameter-number`,
    template: `
        <mat-form-field>
            <input #numberInput matInput type="number" [min]="descriptor.range?.start" [max]="descriptor.range?.end"
                   [step]="descriptor.range?.step" [placeholder]="descriptor.defaultValue" [value]="parameter.value"
                   (change)="onChange($event.target.value)">
            <mat-label>{{descriptor.displayName}}</mat-label>
            <button mat-button *ngIf="numberInput.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="numberInput.value='';onChange(null)">
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
           *ngIf="descriptor.range && (numberInput.value % descriptor.range.step !== 0)">
            {{descriptor.displayName}} only allows inputs in {{descriptor.range.step}} - steps starting with {{descriptor.range.start}}.
        </p>
    `
})
export class NumberParameterComponent extends ParameterComponent<NumberParameterDescriptor, NumberParameter> {
    private ref: ParameterRef;

    protected onInit(): void {
        this.ref = this.descriptor.ref;
    }

    private onChange(value: number): void {
        const parameter = new NumberParameter(this.ref, value);
        console.log("changed: ", parameter);
        this.emit(parameter)
    }
}