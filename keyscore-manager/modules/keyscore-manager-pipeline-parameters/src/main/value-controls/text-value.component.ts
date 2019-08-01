import {Component, EventEmitter, HostBinding, Input, Output} from "@angular/core";
import {FormControl} from "@angular/forms";
import {coerceBooleanProperty} from "@angular/cdk/coercion";
import {TextValue} from "../models/value.model";
import {ValueComponent} from "./value-component.interface";

@Component({
    selector: 'ks-text-value-input',
    template: `
        <mat-form-field>
            <input #inputField matInput type="text" [formControl]="inputControl" (change)="onChange()"
                   [placeholder]="'Value'">
            <mat-label>{{label}}</mat-label>
            <button mat-button *ngIf="inputField.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="inputControl.setValue('');inputField.focus();onChange( )">
                <mat-icon>close</mat-icon>
            </button>
        </mat-form-field>
    `
})
export class TextValueComponent implements ValueComponent{

    static nextId = 0;

    @HostBinding() id = `ks-text-value-input-${TextValueComponent.nextId++}`;
    inputControl = new FormControl();

    @Input()
    get value(): TextValue {
        return new TextValue(this.inputControl.value);
    }

    set value(val: TextValue) {
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

    @Output() changed: EventEmitter<TextValue> = new EventEmitter<TextValue>();

    onChange() {
        this.changed.emit(this.value);
    }


}