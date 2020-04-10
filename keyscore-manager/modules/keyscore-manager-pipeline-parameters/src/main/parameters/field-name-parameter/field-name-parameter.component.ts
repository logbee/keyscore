import {Component, Input, ViewChild} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {StringValidatorService} from "../../service/string-validator.service";
import {AutocompleteFilterComponent} from "../../shared-controls/autocomplete-filter.component";
import {
    FieldNameParameter,
    FieldNameParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/field-name-parameter.model";
import {FieldNameHint} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-fields.model";

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
            <mat-hint *ngIf="descriptor.hint !== fieldNameHint.AnyField && showLabel" translate
                      [translateParams]="{hint:descriptor.hint}">
                PARAMETER.FIELD_NAME_HINT
            </mat-hint>
        </mat-form-field>
        <p
            [ngClass]="(descriptor.hint !== fieldNameHint.AnyField && showLabel) ? 'parameter-warn-with-hint':'parameter-warn'"
            *ngIf="descriptor.mandatory && !inputField.value" translate
            [translateParams]="{name:descriptor.displayName}">
            PARAMETER.IS_REQUIRED
        </p>
        <p
            [ngClass]="(descriptor.hint !== fieldNameHint.AnyField && showLabel) ? 'parameter-warn-with-hint':'parameter-warn'"
            *ngIf="!isValid(inputField.value) && descriptor.validator.description">
            {{descriptor.validator.description}}</p>
        <p
            [ngClass]="(descriptor.hint !== fieldNameHint.AnyField && showLabel) ? 'parameter-warn-with-hint':'parameter-warn'"
            *ngIf="!isValid(inputField.value) && !descriptor.validator.description" translate
            [translateParams]="{pattern:descriptor.validator.expression}">
            PARAMETER.FULFILL_PATTERN
        </p>

    `,
    styleUrls: ['../../style/parameter-module-style.scss']
})
export class FieldNameParameterComponent extends ParameterComponent<FieldNameParameterDescriptor, FieldNameParameter> {
    @Input() showLabel: boolean = true;

    @ViewChild('inputField', {static: true}) inputFieldRef: AutocompleteFilterComponent;
    fieldNameHint: typeof FieldNameHint = FieldNameHint;

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

    clear() {
        this.inputFieldRef.clear();
    }

    focus(event: Event) {
        this.inputFieldRef.focus(event);
    }

    onChange(): void {
        this.emit(this.value);
    }

    onEnter(event: Event) {
        this.keyUpEnterEvent.emit(event);
    }

    isValid(value: string) {
        if (!this.descriptor.validator) {
            return true;
        }
        return this.stringValidator.validate(value, this.descriptor.validator);
    }
}
