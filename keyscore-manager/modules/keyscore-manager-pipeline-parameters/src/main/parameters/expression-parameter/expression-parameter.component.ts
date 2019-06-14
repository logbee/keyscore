import {Component} from "@angular/core";
import "../../style/parameter-module-style.scss";

@Component({
    template: `
        <input id="MyId" matInput type="text" placeholder="Hello">
        <mat-form-field>
<!--        <mat-select>-->
<!--            <mat-option value="RegEx">RegEx</mat-option>-->
<!--            <mat-option value="Grok">Grok</mat-option>-->
<!--            <mat-option value="Glob">Glob</mat-option>-->
<!--            <mat-option value="JSONPath">JSONPath</mat-option>-->
<!--        </mat-select>-->
        </mat-form-field>
<!--        <mat-label>Fubar</mat-label>-->
    `
})
export class ExpressionParameterComponent {

}
