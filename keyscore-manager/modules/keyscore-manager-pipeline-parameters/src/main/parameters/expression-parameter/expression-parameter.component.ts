import {Component} from "@angular/core";
import "../../style/parameter-module-style.scss";
import {ParameterComponent} from "../ParameterComponent";
import {ExpressionParameterDescriptor, ExpressionParameter, ExpressionParameterChoice} from "@/../modules/keyscore-manager-models/src/main/parameters/expression-parameter.model";
import {ParameterRef} from "@/../modules/keyscore-manager-models/src/main/common/Ref";

@Component({
    selector: `parameter-expression`,
    template: `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field fxFlex>
                <mat-label>{{descriptor.displayName}} [{{currentChoice.displayName}}]</mat-label>
                <mat-menu #patternMenu="matMenu">
                    <button mat-menu-item *ngFor="let choice of descriptor.choices" (click)="onExpressionTypeChanged(choice)">{{choice.displayName}}</button>
                </mat-menu>
                <button matPrefix mat-icon-button [matMenuTriggerFor]="patternMenu">
                    <mat-icon>more_vert</mat-icon>
                </button>
                <input #expression matInput type="text" placeholder="expression" [value]="parameter.value"
                       (change)="onExpressionChanged(expression.value)">
                <button mat-button *ngIf="expression.value" matSuffix mat-icon-button aria-label="Clear" (click)="expression.value='';onExpressionChanged('')">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>
        </div>
        <p class="parameter-warn" *ngIf="descriptor.mandatory && !expression.value" translate [translateParams]="{name:descriptor.displayName}">
            PARAMETER.IS_REQUIRED
        </p>
    `,
    styleUrls:['../../style/parameter-module-style.scss']
})
export class ExpressionParameterComponent extends ParameterComponent<ExpressionParameterDescriptor, ExpressionParameter> {

    currentChoice: ExpressionParameterChoice;
    private ref: ParameterRef;

    protected onInit(): void {
        this.ref = this.descriptor.ref;
        this.currentChoice = this.findChoice(this.descriptor.choices, this.value.choice);
    }

    onExpressionChanged(value: string): void {
        const parameter = new ExpressionParameter(this.ref, value, this.value.choice);
        this.emit(parameter)
    }

    onExpressionTypeChanged(value: ExpressionParameterChoice): void {
        if (this.currentChoice != value) {
            const parameter = new ExpressionParameter(this.ref, this.value.value, value.name);
            this.currentChoice = value;
            this.emit(parameter)
        }
    }

    private findChoice(choices: ExpressionParameterChoice[], name: string): ExpressionParameterChoice {
        const index = choices.map(value => value.name).indexOf(name);
        if (index >= 0) return choices[index];
        else return choices[0];
    }
}
