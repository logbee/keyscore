import {Component, OnDestroy, OnInit} from "@angular/core";
import "../../style/parameter-module-style.scss";
import {ExpressionParameter, ExpressionParameterDescriptor,} from "./expression-parameter.model";
import {ParameterComponent} from "../ParameterComponent";
import {Subscription} from "rxjs";

@Component({
    template: `
        <div style="display: flex;">
            <mat-label>{{(descriptor$ | async).displayName}}</mat-label>
            <input #expression matInput type="text" placeholder="expression" (input)="onChanged(expression.value, expressionType.value)">
            <mat-select #expressionType (selectionChange)="onChanged(expression.value, expressionType.value)">
                <mat-option *ngFor="let choice of (descriptor$ | async).choices" [value]="choice.name">{{choice.displayName}}</mat-option>
            </mat-select>
        </div>
    `
})
export class ExpressionParameterComponent extends ParameterComponent<ExpressionParameterDescriptor, ExpressionParameter> {

    private ref: string;

    protected onDescriptorChange(descriptor: ExpressionParameterDescriptor): void {
        this.ref = descriptor.ref
    }

    protected onParameterChange(parameter: ExpressionParameter): void {

    }

    private onChanged(expression: string, expressionType: string): void {
        const parameter = new ExpressionParameter(this.ref, expression, expressionType);
        console.log("changed: ", parameter);
        this.emit(parameter)
    }
}
