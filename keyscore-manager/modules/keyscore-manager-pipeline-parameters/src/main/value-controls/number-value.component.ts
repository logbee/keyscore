import {Component, EventEmitter, HostBinding, Input, Output} from "@angular/core";
import {ValueComponent} from "./value-component.interface";
import {FormControl} from "@angular/forms";
import {coerceBooleanProperty} from "@angular/cdk/coercion";
import {NumberValue} from "@/../modules/keyscore-manager-models/src/main/dataset/Value";

@Component({
    selector: 'ks-number-value-input',
    template: `
        <mat-form-field>
            <input #inputField matInput type="number" [formControl]="inputControl" (change)="onChange()" (keyup.enter)="keyUpEnter.emit($event)">
            <mat-label *ngIf="showLabel">{{label}}</mat-label>
            <button mat-button tabindex="-1" *ngIf="inputField.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="inputControl.setValue('');inputField.focus();onChange( )">
                <mat-icon>close</mat-icon>
            </button>
            <mat-icon matSuffix [inline]="true" svgIcon="number-icon"></mat-icon>
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
    @Input() showLabel:boolean = true;

    @Output() changed: EventEmitter<NumberValue> = new EventEmitter<NumberValue>();
    @Output() keyUpEnter:EventEmitter<Event> = new EventEmitter<Event>();

    onChange() {
        this.changed.emit(this.value);
    }


}