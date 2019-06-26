import {Component} from "@angular/core";
import {ParameterRef} from "keyscore-manager-models"
import {ParameterComponent} from "../ParameterComponent";
import {TextParameter, TextParameterDescriptor} from "./text-parameter.model";

@Component({
    selector: `parameter-text`,
    template: `
        <mat-form-field>
            <input matInput type="text" [placeholder]="(descriptor$ | async).defaultValue"
                   [id]="(parameter$ | async).ref.id" (change)="onChange($event.target.value)" [value]="(parameter$ | async).value">
            <mat-label>{{(descriptor$ | async).displayName}}</mat-label>

            <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                <mat-icon>close</mat-icon>
            </button>
        </mat-form-field>`,

})
export class TextParameterComponent extends ParameterComponent<TextParameterDescriptor, TextParameter> {
    private ref: ParameterRef;

    protected onDescriptorChange(descriptor: TextParameterDescriptor): void {
        this.ref = descriptor.ref;
    }

    private onChange(value: string) {
        const parameter = new TextParameter(this.ref, value);
        console.log("changed: ", parameter);
        this.emit(parameter);
    }

}