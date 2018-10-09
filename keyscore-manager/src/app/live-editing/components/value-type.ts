import {Component, Input} from "@angular/core";
import {ValueJsonClass} from "../../models/dataset/Value";

@Component({
    selector: "value-type",
    template: `
        <ng-container [ngSwitch]="type">
            <div *ngSwitchCase="jsonClass.TextValue">
                <mat-icon>text_format</mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.BooleanValue">
                <mat-icon>keyboard_capslock</mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.NumberValue">
                <mat-icon>filter_1</mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.DecimalValue">
                <mat-icon>child_care</mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.TimestampValue">
                <mat-icon>access_time</mat-icon>
            </div>
            <div *ngSwitchCase="jsonClass.DurationValue">
                <mat-icon>av_timer</mat-icon>
            </div>
        </ng-container>
    `
})
export class ValueType {
    @Input() public type: ValueJsonClass;
    public jsonClass: typeof ValueJsonClass = ValueJsonClass;

}

