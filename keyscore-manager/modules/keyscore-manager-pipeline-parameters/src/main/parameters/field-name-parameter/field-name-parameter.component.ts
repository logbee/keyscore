import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {FieldNameParameter, FieldNameParameterDescriptor} from "./field-name-parameter.model";
import {StringValidatorService} from "../../service/string-validator.service";
import {FieldNameHint} from "keyscore-manager-models";
import {TextParameter} from "../text-parameter/text-parameter.model";
import {AutocompleteFilterComponent} from "../../shared-controls/autocomplete-filter.component";

@Component({
    selector: 'parameter-fieldname',
    template: `
        <mat-form-field>
            <ks-autocomplete-input #inputField [value]="parameter.value"
                                   [options]="autoCompleteDataList"
                                   (change)="onChange()" (keyUpEnterEvent)="onEnter($event)">
            </ks-autocomplete-input>
            <mat-label *ngIf="showLabel">{{label || descriptor.displayName}}</mat-label>
            <button mat-button *ngIf="inputField.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="clear()">
                <mat-icon>close</mat-icon>
            </button>
            <mat-hint *ngIf="descriptor.hint !== fieldNameHint.AnyField && showLabel">You should choose a
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
    @Input() showLabel: boolean = true;

    @ViewChild('inputField') inputFieldRef: AutocompleteFilterComponent;
    public fieldNameHint: typeof FieldNameHint = FieldNameHint;

    get value(): FieldNameParameter {
        return new FieldNameParameter(this.descriptor.ref, this.inputFieldRef.value);
    }

    constructor(private stringValidator: StringValidatorService) {
        super();
    }

    protected onInit() {
        if (this.descriptor.hint === FieldNameHint.AbsentField) {
            this.autoCompleteDataList = [];
        }
    }

    public clear() {
        this.inputFieldRef.clear();
    }

    private onChange(): void {
        this.emit(this.value);
    }

    private onEnter(event: Event) {
        this.keyUpEnterEvent.emit(event);
    }

    private isValid(value: string) {
        if (!this.descriptor.validator) {
            return true;
        }
        return this.stringValidator.validate(value, this.descriptor.validator);
    }
}