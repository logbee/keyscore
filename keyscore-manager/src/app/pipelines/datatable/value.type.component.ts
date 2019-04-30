import {Component, Input} from "@angular/core";
import {ValueJsonClass} from "keyscore-manager-models";

@Component({
    selector: "value-type",
    template: `
        <ng-container [ngSwitch]="type">
            <div *ngSwitchCase="jsonClass.TextValue" matTooltip="TextValue">
                <mat-icon svgIcon="text-icon"></mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.BooleanValue" matTooltip="BooleanValue">
                <mat-icon svgIcon="boolean-icon"></mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.NumberValue" matTooltip="NumberValue">
                <mat-icon svgIcon="number-icon">-icon</mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.DecimalValue" matTooltip="DecimalValue">
                <mat-icon svgIcon="decimal-icon"></mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.TimestampValue" matTooltip="TimestampValue">
                <mat-icon svgIcon="timestamp-icon"></mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.DurationValue" matTooltip="DurationValue">
                <mat-icon svgIcon="duration-icon"></mat-icon>
            </div>
        </ng-container>
    `
})

export class ValueType {
    @Input() public type: ValueJsonClass;
    public jsonClass: typeof ValueJsonClass = ValueJsonClass;
}

