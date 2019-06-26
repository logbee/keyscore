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
                       (change)="onChanged(expression.value, expressionType.value)">
            </mat-form-field>
            <mat-form-field fxFlex>
                <mat-label>Pattern Type</mat-label>
                <mat-select #expressionType (selectionChange)="onChanged(expression.value, expressionType.value)">
                    <mat-option *ngFor="let choice of descriptor.choices" [value]="choice.name">
                        {{choice.displayName}}
                    </mat-option>
                </mat-select>
            </mat-form-field>
        </div>
    `
})
export class ExpressionParameterComponent extends ParameterComponent<ExpressionParameterDescriptor, ExpressionParameter> {

    private ref: ParameterRef;

    protected onInit(): void {
        this.ref = this.descriptor.ref
    }

    private onChanged(expression: string, expressionType: string): void {
        const parameter = new ExpressionParameter(this.ref, expression, expressionType);
        console.log("changed: ", parameter);
        this.emit(parameter)
    }
}
