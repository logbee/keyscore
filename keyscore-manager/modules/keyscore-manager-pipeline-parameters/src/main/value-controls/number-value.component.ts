import {Component, EventEmitter, HostBinding, Input, Output} from "@angular/core";
import {ValueComponent} from "./value-component.interface";
import {FormControl} from "@angular/forms";
import {NumberValue} from "../models/value.model";
import {coerceBooleanProperty} from "@angular/cdk/coercion";

@Component({
    selector: 'ks-number-value-input',
    template: `
        <mat-form-field>
            <input #inputField matInput type="number" [formControl]="inputControl" (change)="onChange()"
                   [placeholder]="'Value'">
            <mat-label>{{label}}</mat-label>
            <mat-icon matSuffix [inline]="true" svgIcon="number-icon"></mat-icon>
            <button mat-button *ngIf="inputField.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="inputControl.setValue('');inputField.focus();onChange( )">
                <mat-icon>close</mat-icon>
            </button>
        </mat-form-field>
    `
})
export class NumberValueComponent<T> implements ValueComponent{

    static nextId = 0;

    @HostBinding() id = `ks-text-value-input-${NumberValueComponent.nextId++}`;
    inputControl = new FormControl();

    @Input()
    get value(): NumberValue {
        return new NumberValue(Math.round(this.inputControl.value));
    }

    set value(val: NumberValue) {
        this.inputControl.setValue(val.value);
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

    @Output() changed: EventEmitter<NumberValue> = new EventEmitter<NumberValue>();

    onChange() {
        this.changed.emit(this.value);
    }


}