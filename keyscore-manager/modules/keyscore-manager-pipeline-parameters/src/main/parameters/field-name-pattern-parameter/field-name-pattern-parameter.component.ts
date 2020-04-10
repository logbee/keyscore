import {Component} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {
    FieldNamePatternParameter,
    FieldNamePatternParameterDescriptor,
    PatternTypeChoice
} from "@/../modules/keyscore-manager-models/src/main/parameters/field-name-pattern-parameter.model";
import {
    FieldNameHint,
    PatternType
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-fields.model";

@Component({
    selector: 'parameter-field-name-pattern',
    template: `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field fxFlex="50">
                <ks-autocomplete-input #fieldName [options]="autoCompleteDataList"
                                       [placeholder]="'Field Name / Pattern'"
                                       [value]="parameter.value"
                                       (change)="onChange(fieldName.value,patternType.value)">
                </ks-autocomplete-input>
                <mat-label>{{descriptor.displayName}}</mat-label>
                <button mat-button *ngIf="fieldName.value" matSuffix mat-icon-button aria-label="Clear"
                        (click)="fieldName.value='';onChange('',patternType.value);fieldName.focus($event)">
                    <mat-icon>close</mat-icon>
                </button>
                <mat-hint *ngIf="descriptor.hint !== fieldNameHint.AnyField" translate
                          [translateParams]="{hint:descriptor.hint}">
                    PARAMETER.FIELD_NAME_HINT
                </mat-hint>
            </mat-form-field>
            <mat-form-field fxFlex="50">
                <mat-label>Pattern</mat-label>
                <mat-select #patternType [value]="parameter.patternType"
                            (selectionChange)="onChange(fieldName.value, patternType.value)">
                    <mat-option *ngFor="let pattern of descriptor.supports" [value]="pattern.type">
                        {{pattern.displayName}}
                    </mat-option>
                </mat-select>
            </mat-form-field>
        </div>
        <p class="parameter-warn-with-hint" *ngIf="descriptor.mandatory && (!fieldName.value || !patternType.value)"
           translate [translateParams]="{name:descriptor.displayName}">
            PARAMETER.IS_REQUIRED
        </p>
    `,
    styleUrls: ['../../style/parameter-module-style.scss']

})
export class FieldNamePatternParameterComponent extends ParameterComponent<FieldNamePatternParameterDescriptor, FieldNamePatternParameter> {
    public fieldNameHint: typeof FieldNameHint = FieldNameHint;

    protected onInit() {
        if (this.descriptor.hint === FieldNameHint.AbsentField) {
            this.autoCompleteDataList = [];
        }
    }

    onChange(fieldName: string, patternType: PatternType): void {
        const parameter = new FieldNamePatternParameter(this.descriptor.ref, fieldName, patternType);
        this.emit(parameter)
    }

    comparePatternTypes(p0: PatternTypeChoice, p1: PatternTypeChoice) {
        return p0.type === p1.type;
    }

}
