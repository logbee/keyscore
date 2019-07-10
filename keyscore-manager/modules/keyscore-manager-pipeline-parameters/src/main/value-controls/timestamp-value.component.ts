import {Component, EventEmitter, HostBinding, Input, OnInit, Output} from "@angular/core";
import {FormControl} from "@angular/forms";
import {coerceBooleanProperty} from "@angular/cdk/coercion";
import {TimestampValue} from "../models/value.model";
import * as moment from "moment-timezone";
import {ValueComponent} from "./value-component.interface";

@Component({
    selector: 'ks-timestamp-value-input',
    template: `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field fxFlex="50">
                <input #inputField matInput type="datetime-local" [formControl]="inputControl" (change)="onChange()"
                       [placeholder]="'Value'" step="1">
                <mat-label>{{label}}</mat-label>
                <button mat-button *ngIf="inputField.value" matSuffix mat-icon-button aria-label="Clear"
                        (click)="inputControl.setValue('');onChange( )">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>
            <mat-form-field fxFlex>
                <mat-label>Timezone</mat-label>
                <mat-select [formControl]="selectControl" (selectionChange)="onChange()">
                    <mat-option *ngFor="let timezone of timeZones" [value]="timezone">{{timezone}}</mat-option>
                </mat-select>
            </mat-form-field>
        </div>
    `
})
export class TimestampValueComponent implements ValueComponent {

    static nextId = 0;

    @HostBinding() id = `ks-timestamp-value-input-${TimestampValueComponent.nextId++}`;
    inputControl = new FormControl();
    selectControl = new FormControl(moment.tz.guess());

    @Input()
    get value(): TimestampValue {
        let date = moment.tz(this.inputControl.value, this.selectControl.value).format();
        let seconds = Date.parse(date) / 1000;
        return new TimestampValue(seconds, 0);
    }

    set value(val: TimestampValue) {
        let date = moment.tz(val.seconds * 1000, this.selectControl.value).format().substring(0, 19);
        this.inputControl.setValue(date);
        this.onChange();
    }

    @Input()
    get disabled(): boolean {
        return this._disabled;
    }

    set disabled(value: boolean) {
        this._disabled = coerceBooleanProperty(value);
        this._disabled ? this.disable() : this.enable();
    }

    private _disabled = false;

    @Input() label: string = 'Value';

    @Output() changed: EventEmitter<TimestampValue> = new EventEmitter<TimestampValue>();

    private timeZones: string[] = moment.tz.names();


    onChange() {
        this.changed.emit(this.value);
    }

    private disable() {
        this.inputControl.disable();
        this.selectControl.disable();
    }

    private enable() {
        this.inputControl.enable();
        this.selectControl.enable();
    }


}