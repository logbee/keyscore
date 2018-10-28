import {Component, Input} from "@angular/core";
import {ValueJsonClass} from "../../models/dataset/Value";

@Component({
    selector: "value-type",
    template: `
        <ng-container [ngSwitch]="type">
            <div *ngSwitchCase="jsonClass.TextValue" matTooltip="TextValue">
                <mat-icon>text-icon</mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.BooleanValue" matTooltip="BooleanValue">
                <mat-icon>boolean-icon</mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.NumberValue" matTooltip="NumberValue">
                <mat-icon>number-icon</mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.DecimalValue" matTooltip="DecimalValue">
                <mat-icon>decimal-icon</mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.TimestampValue" matTooltip="TimestampValue">
                <mat-icon>timestamp-icon</mat-icon>m
            </div>
            <div *ngSwitchCase="jsonClass.DurationValue" matTooltip="DurationValue">
                <mat-icon>duration-icon</mat-icon>
            </div>
        </ng-container>
    `
})
export class ValueType {
    @Input() public type: ValueJsonClass;
    public jsonClass: typeof ValueJsonClass = ValueJsonClass;

}

