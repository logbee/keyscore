import {Component} from "@angular/core";
import {ParameterRef} from "keyscore-manager-models"
import {ParameterComponent} from "../ParameterComponent";
import {TextParameter, TextParameterDescriptor} from "./text-parameter.model";

@Component({
    selector: `parameter-text`,
    template: `
        <mat-form-field>
            <input #textInput matInput type="text" [placeholder]="descriptor.defaultValue"
                   [id]="parameter.ref.id" (change)="onChange($event.target.value)"
                   [value]="parameter.value">
            <mat-label>{{descriptor.displayName}}</mat-label>

            <button mat-button *ngIf="textInput.value" matSuffix mat-icon-button aria-label="Clear" (click)="textInput.value='';onChange('')">
                <mat-icon>close</mat-icon>
            </button>
        </mat-form-field>`,

})
export class TextParameterComponent extends ParameterComponent<TextParameterDescriptor, TextParameter> {
    private ref: ParameterRef;

    protected onInit(): void {
        this.ref = this.descriptor.ref;
    }

    private onChange(value: string) {
        const parameter = new TextParameter(this.ref, value);
        console.log("changed: ", parameter);
        this.emit(parameter);
    }

}