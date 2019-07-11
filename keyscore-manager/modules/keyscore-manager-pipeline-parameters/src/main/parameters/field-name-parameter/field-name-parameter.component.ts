import {Component} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {FieldNameParameter, FieldNameParameterDescriptor} from "./field-name-parameter.model";
import {StringValidatorService} from "../../service/string-validator.service";
import {FieldNameHint} from "keyscore-manager-models";

@Component({
    selector: 'parameter-fieldname',
    template: `
        <mat-form-field>
            <ks-autocomplete-input #inputField [value]="parameter.value"
                                   [placeholder]="'Field Name'" [options]="autoCompleteDataList"
                                   (change)="onChange(inputField.value)">
            </ks-autocomplete-input>
            <mat-label>{{descriptor.displayName}}</mat-label>
            <button mat-button *ngIf="inputField.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="inputField.value='';onChange('');">
                <mat-icon>close</mat-icon>
            </button>
            <mat-hint *ngIf="descriptor.hint !== fieldNameHint.AnyField">You should choose a
                {{descriptor.hint}}
            </mat-hint>
        </mat-form-field>
        <p class="parameter-warn-with-hint" *ngIf="descriptor.mandatory && !inputField.value">{{descriptor.displayName}}
            is
            required!
        </p>
        <p class="parameter-warn-with-hint" *ngIf="!isValid(inputField.value) && descriptor.validator.description">
            {{descriptor.validator.description}}</p>
        <p class="parameter-warn-with-hint" *ngIf="!isValid(inputField.value) && !descriptor.validator.description">
            Your Input has to fulfill the following Pattern: {{descriptor.validator.expression}}</p>

    `
})
export class FieldNameParameterComponent extends ParameterComponent<FieldNameParameterDescriptor, FieldNameParameter> {

    public fieldNameHint: typeof FieldNameHint = FieldNameHint;

    constructor(private stringValidator: StringValidatorService) {
        super();
    }

    protected onInit() {
        if (this.descriptor.hint === FieldNameHint.AbsentField) {
            this.autoCompleteDataList = [];
        }
    }

    private onChange(value: string): void {
        const parameter = new FieldNameParameter(this.descriptor.ref, value);
        console.log("changed: ", parameter);
        this.emit(parameter);
    }

    private isValid(value: string) {
        if (!this.descriptor.validator) {
            return true;
        }
        return this.stringValidator.validate(value, this.descriptor.validator);
    }
}