import {Component, Input, OnInit} from "@angular/core";
import {ParameterControlService} from "../service/parameter-control.service";
import {Parameter,ResolvedParameterDescriptor} from "keyscore-manager-models"

@Component({
    selector:`text-parameter`,
    template:`<mat-form-field *ngSwitchCase="jsonClass.TextParameterDescriptor">
        <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
               [formControlName]="directiveInstance || parameter.ref.id"
               [id]="directiveInstance || parameter.ref.id">
        <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>

        <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
            <mat-icon>close</mat-icon>
        </button>
    </mat-form-field>`,

})
export class TextParameterComponent implements OnInit{
    @Input() parameter:Parameter;
    @Input() descriptor:ResolvedParameterDescriptor;

    constructor(private controlFactory:ParameterControlService){

    }

    public ngOnInit(){

    }

}