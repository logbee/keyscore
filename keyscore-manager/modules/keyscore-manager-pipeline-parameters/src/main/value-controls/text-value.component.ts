import {Component, EventEmitter, HostBinding, Input, Output} from "@angular/core";
import {FormControl} from "@angular/forms";
import {coerceBooleanProperty} from "@angular/cdk/coercion";
import {TextValue} from "@keyscore-manager-models";
import {ValueComponent} from "./value-component.interface";

@Component({
    selector: 'ks-text-value-input',
    template: `
        <mat-form-field>
            <input #inputField matInput type="text" [formControl]="inputControl" (change)="onChange()" (keyup.enter)="keyUpEnter.emit($event)">
            <mat-label *ngIf="showLabel">{{label}}</mat-label>
            <button mat-button *ngIf="inputField.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="inputControl.setValue('');inputField.focus();onChange( )">
                <mat-icon>close</mat-icon>
            </button>
            <mat-icon matSuffix [inline]="true" svgIcon="text-icon"></mat-icon>
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
    @Input() showLabel:boolean = true;

    @Output() changed: EventEmitter<TextValue> = new EventEmitter<TextValue>();
    @Output() keyUpEnter:EventEmitter<Event> = new EventEmitter<Event>();

    onChange() {
        this.changed.emit(this.value);
    }


}