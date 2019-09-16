import {Component} from "@angular/core";
import "../../style/parameter-module-style.scss";
import {ParameterComponent} from "../ParameterComponent";
import {ExpressionParameterDescriptor, ExpressionParameter} from "@/../modules/keyscore-manager-models/src/main/parameters/expression-parameter.model";
import {ParameterRef} from "@/../modules/keyscore-manager-models/src/main/common/Ref";

@Component({
    selector: `parameter-expression`,
    template: `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field fxFlex="80">
                <mat-label>{{descriptor.displayName}}</mat-label>
                <input #expression matInput type="text" placeholder="expression" [value]="parameter.value"
                       (change)="onChange(expression.value, expressionType.value)">
                <button mat-button *ngIf="expression.value" matSuffix mat-icon-button aria-label="Clear" (click)="expression.value='';onChange('',expressionType.value)">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>
            <mat-form-field fxFlex>
                <mat-label>Pattern</mat-label>
                <mat-select #expressionType (selectionChange)="onChange(expression.value, expressionType.value)">
                    <mat-option *ngFor="let choice of descriptor.choices" [value]="choice.name">
                        {{choice.displayName}}
                    </mat-option>
                </mat-select>
            </mat-form-field>
        </div>
        <p class="parameter-warn" *ngIf="descriptor.mandatory && !expression.value" translate [translateParams]="{name:descriptor.displayName}">
            PARAMETER.IS_REQUIRED
        </p>
        <p class="parameter-warn" *ngIf="descriptor.mandatory && !expressionType.value" translate [translateParams]="{name:'Pattern'}">
            PARAMETER.IS_REQUIRED
        </p>
        
    `,
    styleUrls:['../../style/parameter-module-style.scss']
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
