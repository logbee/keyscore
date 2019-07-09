import {Component} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {StringValidatorService} from "../../service/string-validator.service";
import {FieldParameter, FieldParameterDescriptor} from "./field-parameter.model";
import {Field, Value} from "../../models/value.model";

@Component({
    selector: `parameter-field`,
    template: `
        <mat-form-field>
            <input #fieldInput matInput type="text" [placeholder]="descriptor.defaultValue"
                   (change)="onChange($event.target.value)"
                   [value]="parameter.value">
            <mat-label>{{descriptor.displayName}}</mat-label>

            <button mat-button *ngIf="fieldInput.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="fieldInput.value='';onChange('')">
                <mat-icon>close</mat-icon>
            </button>
        </mat-form-field>
        <p class="parameter-required" *ngIf="descriptor.mandatory && !fieldInput.value">{{descriptor.displayName}} is
            required!</p>
        <p class="parameter-warn" *ngIf="!isValid(fieldInput.value) && descriptor.nameValidator.description">{{descriptor.nameValidator.description}}</p>
        <p class="parameter-warn" *ngIf="!isValid(fieldInput.value) && !descriptor.nameValidator.description">Your Input has to fulfill the following Pattern:
            {{descriptor.nameValidator.expression}}</p>
    `,

})
export class FieldParameterComponent extends ParameterComponent<FieldParameterDescriptor, FieldParameter> {

    constructor(private stringValidator: StringValidatorService) {
        super();
    }


    private onChange(fieldName: string,value:Value): void {
        const parameter = new FieldParameter(this.descriptor.ref, new Field(fieldName,value));
        console.log("changed: ", parameter);
        this.emit(parameter);
    }

    private isValid(value: string): boolean {
        if (!this.descriptor.nameValidator) {
            return true;
        }
        return this.stringValidator.validate(value, this.descriptor.nameValidator);


    }
}