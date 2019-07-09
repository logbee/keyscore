import {Component, EventEmitter, HostBinding, Input, Output} from "@angular/core";
import {FormControl} from "@angular/forms";
import {coerceBooleanProperty} from "@angular/cdk/coercion";
import {TimestampValue} from "../models/value.model";

@Component({
    selector: 'ks-timestamp-value-input',
    template: `
        <mat-form-field>
            <input #inputField matInput type="datetime-local" [formControl]="inputControl" (change)="onChange()"
                   [placeholder]="'Value'" step="1">
            <mat-label>{{label}}</mat-label>
            <button mat-button *ngIf="inputField.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="inputControl.setValue('');onChange( )">
                <mat-icon>close</mat-icon>
            </button>
        </mat-form-field>
    `
})
export class TimestampValueComponent {

    static nextId = 0;

    @HostBinding() id = `ks-timestamp-value-input-${TimestampValueComponent.nextId++}`;
    inputControl = new FormControl();

    @Input()
    get value(): TimestampValue {
        let seconds = Date.parse(this.inputControl.value) / 1000;
        return new TimestampValue(seconds, 0);
    }

    set value(val: TimestampValue) {
        let date: Date = new Date(val.seconds * 1000);
        let iso:string = date.toISOString().split('.')[0].concat("Z");

        console.log(iso);
        this.inputControl.setValue(iso);
    }

    @Input()
    get disabled(): boolean {
        return this._disabled;
    }

    set disabled(value: boolean) {
        this._disabled = coerceBooleanProperty(value);
        this._disabled ? this.inputControl.disable() : this.inputControl.enable();
    }

    private _disabled = false;

    @Input() label: string = 'Value';

    @Output() changed: EventEmitter<TimestampValue> = new EventEmitter<TimestampValue>();

    onChange() {
        this.changed.emit(this.value);
    }


}