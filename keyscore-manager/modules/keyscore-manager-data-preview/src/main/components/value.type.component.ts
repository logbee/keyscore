import {Component, Input} from "@angular/core";
import {ValueJsonClass, Value, MimeType, TextValue, BinaryValue} from "@/../modules/keyscore-manager-models/src/main/dataset/Value";

@Component({
    selector: "value-type",
    template: `
        <ng-container [ngSwitch]="type">
            <div *ngSwitchCase="jsonClass.TextValue">
                <mat-icon svgIcon="text-icon" matTooltip="{{this.getToolTip()}}" matTooltipPosition="right"></mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.BooleanValue">
                <mat-icon svgIcon="boolean-icon" matTooltip="{{this.getToolTip()}}" matTooltipPosition="right"></mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.NumberValue">
                <mat-icon svgIcon="number-icon" matTooltip="{{this.getToolTip()}}" matTooltipPosition="right"></mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.DecimalValue">
                <mat-icon svgIcon="decimal-icon" matTooltip="{{this.getToolTip()}}" matTooltipPosition="right"></mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.TimestampValue">
                <mat-icon svgIcon="timestamp-icon" matTooltip="{{this.getToolTip()}}" matTooltipPosition="right"></mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.DurationValue">
                <mat-icon svgIcon="duration-icon" matTooltip="{{this.getToolTip()}}" matTooltipPosition="right"></mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.BinaryValue">
                <mat-icon matTooltip="{{this.getToolTip()}}" matTooltipPosition="right">grain</mat-icon>
            </div>
        </ng-container>
    `
})

export class ValueType {

    private _value: Value;
    private type: ValueJsonClass;
    private jsonClass: typeof ValueJsonClass = ValueJsonClass;

    @Input() public get value(): Value { return this._value }

    public set value(value: Value) {
        this._value = value;
        this.type = value.jsonClass;
    }

    public getToolTip(): string {
        switch (this._value.jsonClass) {
            case ValueJsonClass.BooleanValue:
                return "Boolean";
            case ValueJsonClass.NumberValue:
                return "Number";
            case ValueJsonClass.DecimalValue:
                return "Decimal";
            case ValueJsonClass.TextValue:
                if (this._value.mimetype) {
                    return this._value.mimetype.primary + "/" + this._value.mimetype.sub
                } else {
                    return "Text"
                }
            case ValueJsonClass.TimestampValue:
                return "Timestamp";
            case ValueJsonClass.DurationValue:
                return "Duration";
            case ValueJsonClass.BinaryValue:
                if (this._value.mimetype) {
                    return this._value.mimetype.primary + "/" + this._value.mimetype.sub
                } else {
                    return "Binary"
                }
        }

        return "";
    }
}

