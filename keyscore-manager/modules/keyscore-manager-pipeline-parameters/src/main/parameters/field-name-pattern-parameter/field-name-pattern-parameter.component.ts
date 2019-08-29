import {Component} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {
    FieldNamePatternParameter,
    FieldNamePatternParameterDescriptor,
    PatternType
} from "./field-name-pattern-parameter.model";
import {FieldNameHint} from "@keyscore-manager-models";

@Component({
    selector: 'parameter-field-name-pattern',
    template: `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field fxFlex="80">
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
                <mat-hint *ngIf="descriptor.hint !== fieldNameHint.AnyField">You should choose a {{descriptor.hint}}</mat-hint>
            </mat-form-field>
            <mat-form-field fxFlex>
                <mat-label>Pattern Type</mat-label>
                <mat-select #patternType (selectionChange)="onChange(fieldName.value, patternType.value)">
                    <mat-option *ngFor="let pattern of descriptor.supports" [value]="pattern.type">
                        {{pattern.displayName}}
                    </mat-option>
                </mat-select>
            </mat-form-field>
        </div>
        <p class="parameter-warn-with-hint" *ngIf="descriptor.mandatory && (!fieldName.value || !patternType.value)">
            {{descriptor.displayName}} is required!</p>`
})
export class FieldNamePatternParameterComponent extends ParameterComponent<FieldNamePatternParameterDescriptor, FieldNamePatternParameter> {
    public fieldNameHint: typeof FieldNameHint = FieldNameHint;

    protected onInit() {
        if (this.descriptor.hint === FieldNameHint.AbsentField) {
            this.autoCompleteDataList = [];
        }
    }

    private onChange(fieldName: string, patternType: PatternType): void {
        const parameter = new FieldNamePatternParameter(this.descriptor.ref, fieldName, patternType);
        console.log("changed: ", parameter);
        this.emit(parameter)
    }
}