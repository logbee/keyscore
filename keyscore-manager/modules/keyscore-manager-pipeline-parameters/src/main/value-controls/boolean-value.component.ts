import {Component, EventEmitter, HostBinding, Input, Output} from "@angular/core";
import {FormControl} from "@angular/forms";
import {coerceBooleanProperty} from "@angular/cdk/coercion";
import {BooleanValue} from "../models/value.model";
import {ValueComponent} from "./value-component.interface";

@Component({
    selector: 'ks-boolean-value-input',
    template: `
        <div fxLayout="row" fxFill fxLayoutAlign="start center">
            <mat-slide-toggle  [formControl]="slideControl" (change)="onChange()">
                <span *ngIf="showLabel">{{label}}</span>
            </mat-slide-toggle>
        </div>`
})
export class BooleanValueComponent implements ValueComponent {

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
    @Input() showLabel:boolean = true;

    @Output() changed: EventEmitter<BooleanValue> = new EventEmitter<BooleanValue>();
    @Output() keyUpEnter:EventEmitter<Event> = new EventEmitter<Event>();


    private _disabled = false;

    onChange() {
        this.changed.emit(this.value);
    }


}