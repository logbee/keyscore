import {Component} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {FieldNameParameter, FieldNameParameterDescriptor} from "./fieldname-parameter.model";
import {StringValidatorService} from "../../service/string-validator.service";
import {FieldNameHint} from "keyscore-manager-models";

@Component({
    selector: 'parameter-fieldname',
    template: `
        <mat-form-field>
            <input #inputField matInput type="text"
                   [placeholder]="descriptor.defaultValue" [matAutocomplete]="auto"
                   (change)="onChange($event.target.value)">
            <mat-label>{{descriptor.displayName}} ({{descriptor.hint}})</mat-label>
            <mat-autocomplete #auto="matAutocomplete">
                <mat-option *ngFor="let item of autoCompleteDataList" [value]="item">{{item}}</mat-option>
            </mat-autocomplete>
        </mat-form-field>
        <p class="parameter-required" *ngIf="descriptor.mandatory && !inputField.value">{{descriptor.displayName}} is
            required!</p>
        <p class="parameter-warn" *ngIf="!isValid(inputField.value) && descriptor.validator.description">{{descriptor.validator.description}}</p>
        <p class="parameter-warn" *ngIf="!isValid(inputField.value) && !descriptor.validator.description">Your Input has to fulfill the following Pattern:
            {{descriptor.validator.expression}}</p>
    `
})
export class FieldNameParameterComponent extends ParameterComponent<FieldNameParameterDescriptor, FieldNameParameter> {

    constructor(private stringValidator: StringValidatorService) {
        super();
    }

    protected onInit(){
        if(this.descriptor.hint === FieldNameHint.AbsentField){
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