import {ParameterComponent} from "../ParameterComponent";
import {Component} from "@angular/core";
import {ParameterRef} from "@keyscore-manager-models";
import {NumberParameter, NumberParameterDescriptor} from "./number-parameter.model";

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
            <p class="parameter-warn" *ngIf="descriptor.mandatory && !numberInput.value">{{descriptor.displayName}} is
                required!</p>
            <p class="parameter-warn"
               *ngIf="!templateNumber.isInteger(+numberInput.value)">
                {{descriptor.displayName}} hast to be be an integer. Floating point values will be rounded.
            </p>
        </ng-template>

        <ng-template #range>
            <ks-slider [label]="descriptor.displayName" [range]="descriptor.range"
                       [value]="parameter.value" (changed)="onChange($event)"></ks-slider>
        </ng-template>

    `
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