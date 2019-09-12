import {ParameterComponent} from "../ParameterComponent";
import {Component} from "@angular/core";
import {DecimalParameterDescriptor, DecimalParameter} from "@/../modules/keyscore-manager-models/src/main/parameters/decimal-parameter.model";
import {ParameterRef} from "@/../modules/keyscore-manager-models/src/main/common/Ref";

@Component({
    selector: ``,
    template: `
        <div *ngIf="descriptor.range;then range else noRange"></div>
        <ng-template #noRange>
            <mat-form-field>
                <input #numberInput matInput type="number" [placeholder]="descriptor.defaultValue"
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
        </ng-template>

        <ng-template #range>
            <ks-slider [label]="descriptor.displayName" [range]="descriptor.range"
                       [value]="parameter.value" (changed)="onChange($event.toString())"></ks-slider>
        </ng-template>
    `,
    styleUrls:['../../style/parameter-module-style.scss']
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


}