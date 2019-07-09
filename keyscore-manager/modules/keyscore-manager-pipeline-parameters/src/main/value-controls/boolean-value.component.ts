import {Component, EventEmitter, HostBinding, Input, Output} from "@angular/core";
import {FormControl} from "@angular/forms";
import {coerceBooleanProperty} from "@angular/cdk/coercion";
import {BooleanValue} from "../models/value.model";

@Component({
    selector: 'ks-boolean-value-input',
    template: `
        <mat-slide-toggle [formControl]="slideControl" (change)="onChange()">
            {{label}}
        </mat-slide-toggle>`
})
export class BooleanValueComponent {

    static nextId = 0;

    @HostBinding() id = `ks-boolean-value-input-${BooleanValueComponent.nextId++}`;
    slideControl = new FormControl();


    @Input()
    get value(): BooleanValue {
        return new BooleanValue(this.slideControl.value);
    }

    set value(val: BooleanValue) {
        this.slideControl.setValue(val.value);
    }

    @Input()
    get disabled(): boolean {
        return this._disabled;
    }

    set disabled(value: boolean) {
        this._disabled = coerceBooleanProperty(value);
        this._disabled ? this.slideControl.disable() : this.slideControl.enable();
    }

    @Input() label: string = 'Value';

    @Output() changed: EventEmitter<BooleanValue> = new EventEmitter<BooleanValue>();

    private _disabled = false;

    onChange() {
        this.changed.emit(this.value);
    }


}