import {Component} from "@angular/core";
import "../../style/parameter-module-style.scss";
import {ExpressionParameter, ExpressionParameterDescriptor,} from "./expression-parameter.model";
import {ParameterComponent} from "../ParameterComponent";
import {ParameterRef} from "keyscore-manager-models";

@Component({
    selector: `parameter-expression`,
    template: `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field fxFlex="80">
                <mat-label>{{descriptor.displayName}}</mat-label>
                <input #expression matInput type="text" placeholder="expression"
                       (change)="onChange(expression.value, expressionType.value)">
                <button mat-button *ngIf="expression.value" matSuffix mat-icon-button aria-label="Clear" (click)="expression.value='';onChange('',expressionType.value)">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>
            <mat-form-field fxFlex>
                <mat-label>Pattern Type</mat-label>
                <mat-select #expressionType (selectionChange)="onChange(expression.value, expressionType.value)">
                    <mat-option *ngFor="let choice of descriptor.choices" [value]="choice.name">
                        {{choice.displayName}}
                    </mat-option>
                </mat-select>
            </mat-form-field>
        </div>
        <p class="parameter-required" *ngIf="descriptor.mandatory && (!expression.value || !expressionType.value)">{{descriptor.displayName}} is
            required!</p>
    `
})
export class ExpressionParameterComponent extends ParameterComponent<ExpressionParameterDescriptor, ExpressionParameter> {

    private ref: ParameterRef;

    protected onInit(): void {
        this.ref = this.descriptor.ref
    }

    private onChange(expression: string, expressionType: string): void {
        const parameter = new ExpressionParameter(this.ref, expression, expressionType);
        console.log("changed: ", parameter);
        this.emit(parameter)
    }
}