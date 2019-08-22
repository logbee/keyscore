import {ValueComponent} from "./value-component.interface";
import {Component, EventEmitter, HostBinding, Input, Output, ViewChild} from "@angular/core";
import {DurationValue} from "../models/value.model";
import {coerceBooleanProperty} from "@angular/cdk/coercion";
import {DurationInputComponent} from "../shared-controls/duration-input.component";

@Component({
    selector: 'ks-duration-value-input',
    template: `
        <mat-form-field>
            <ks-duration-input #durationInput (changed)="onChange($event)"></ks-duration-input>
            <mat-label *ngIf="showLabel">{{label}}</mat-label>
            <mat-icon matSuffix [inline]="true" svgIcon="duration-icon"></mat-icon>
            <button mat-button *ngIf="durationInput.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="clearInput()">
                <mat-icon>close</mat-icon>
            </button>
        </mat-form-field>
    `,
    styles:[`
        ::ng-deep ks-duration-value-input .mat-form-field-suffix{
            align-self: flex-end !important;
            margin-bottom:2px !important;
        }
        ::ng-deep ks-duration-value-input button.mat-form-field-suffix{
            align-self: flex-end !important;
            margin-bottom:12px !important;
        }
    `]
})
export class DurationValueComponent implements ValueComponent {

    static nextId = 0;

    @HostBinding() id = `ks-text-value-input-${DurationValueComponent.nextId++}`;
    @ViewChild('durationInput') durationInput: DurationInputComponent;

    @Input()
    get value(): DurationValue {
        return new DurationValue(this._value, 0);
    }

    set value(val: DurationValue) {
        this.durationInput.value = val.seconds;
        this._value = val.seconds;
    }

    private _value: number;

    @Input()
    get disabled(): boolean {
        return this._disabled;
    }

    set disabled(value: boolean) {
        this._disabled = coerceBooleanProperty(value);
        this.durationInput.disabled = value;
    }

    private _disabled = false;

    @Input() label: string = 'Duration';
    @Input() showLabel:boolean = true;

    @Output() changed: EventEmitter<DurationValue> = new EventEmitter<DurationValue>();

    onChange(seconds: number) {
        this._value = seconds;
        this.changed.emit(this.value);
    }

    private clearInput(){
        this.value=new DurationValue(0,0);
        this.durationInput.focus();
        this.onChange(0);
    }


}