import {Component} from "@angular/core";
import {ExpressionType, ParameterRef} from "keyscore-manager-models"
import {ParameterComponent} from "../ParameterComponent";
import {TextParameter, TextParameterDescriptor} from "./text-parameter.model";
import * as globmatch from "minimatch";
import {StringValidatorService} from "../../service/string-validator.service";

@Component({
    selector: `parameter-text`,
    template: `
        <mat-form-field>
            <input #textInput matInput type="text" [placeholder]="descriptor.defaultValue"
                   (change)="onChange($event.target.value)"
                   [value]="parameter.value">
            <mat-label>{{descriptor.displayName}}</mat-label>

            <button mat-button *ngIf="textInput.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="textInput.value='';onChange('')">
                <mat-icon>close</mat-icon>
            </button>
        </mat-form-field>
        <p class="parameter-required" *ngIf="descriptor.mandatory && !textInput.value">{{descriptor.displayName}} is
            required!</p>
        <p class="parameter-warn" *ngIf="!isValid(textInput.value)">Your Input has to fulfill the following Pattern:
            {{descriptor.validator.expression}}</p>
    `,

})
export class TextParameterComponent extends ParameterComponent<TextParameterDescriptor, TextParameter> {

    constructor(private stringValidator: StringValidatorService) {
        super();
    }


    private onChange(value: string): void {
        const parameter = new TextParameter(this.descriptor.ref, value);
        console.log("changed: ", parameter);
        this.emit(parameter);
    }

    private isValid(value: string): boolean {
        if (!this.descriptor.validator) {
            return true;
        }
        return this.stringValidator.validate(value, this.descriptor.validator);


    }
}