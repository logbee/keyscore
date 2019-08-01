import {
    Component,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    OnDestroy,
    Optional,
    Output,
    Self
} from "@angular/core";
import {ControlValueAccessor, FormBuilder, FormGroup, NgControl} from "@angular/forms";
import {MatFormFieldControl} from "@angular/material";
import {Subject} from "rxjs";
import {FocusMonitor} from "@angular/cdk/a11y";
import {coerceBooleanProperty} from "@angular/cdk/coercion";

class Duration {
    constructor(
        readonly years: number,
        readonly months: number,
        readonly days: number,
        readonly hours: number,
        readonly minutes: number,
        readonly seconds: number
    ) {
    }
}

@Component({
    selector: 'ks-duration-input',
    template: `
        <div [formGroup]="parts" fxLayout="row">
            <div fxLayout="column" fxLayoutGap="5px">
                <span fxFlexAlign="center" class="input-header">Years</span>
                <div fxLayout="row">
                    <input type="number" min="0" formControlName="years" (change)="onChange()">
                    <span fxLayoutAlign="center">:</span>
                </div>
            </div>
            <div fxLayout="column" fxLayoutGap="5px">
                <span fxFlexAlign="center" class="input-header">Months</span>
                <div fxLayout="row">
                    <input type="number" min="0" formControlName="months" (change)="onChange()">
                    <span fxLayoutAlign="center">:</span>
                </div>
            </div>
            <div fxLayout="column" fxLayoutGap="5px">
                <span fxFlexAlign="center" class="input-header">Days</span>
                <div fxLayout="row">
                    <input type="number" min="0" formControlName="days" (change)="onChange()">
                    <span fxLayoutAlign="center">:</span>
                </div>
            </div>
            <div fxLayout="column" fxLayoutGap="5px">
                <span fxFlexAlign="center" class="input-header">H</span>
                <div fxLayout="row">
                    <input type="number" min="0" formControlName="hours" (change)="onChange()">
                    <span fxLayoutAlign="center">:</span>
                </div>
            </div>
            <div fxLayout="column" fxLayoutGap="5px">
                <span fxFlexAlign="center" class="input-header">Min</span>
                <div fxLayout="row">
                    <input type="number" min="0" formControlName="minutes" (change)="onChange()">
                    <span fxLayoutAlign="center">:</span>
                </div>
            </div>
            <div fxLayout="column" fxLayoutGap="5px">
                <span fxFlexAlign="center" class="input-header">Sec</span>
                <input type="number" min="0" formControlName="seconds" (change)="onChange()">
            </div>
        </div>
    `,
    styles: [`
        div {
            display: flex;
        }

        input {
            border: none;
            background: none;
            padding: 0;
            outline: none;
            font: inherit;
            text-align: end;
            max-width: 4ch;
            opacity: 0;
            transition: opacity 200ms;
        }

        :host.floating input {
            opacity: 1;
        }

        span {
            opacity: 0;
            transition: opacity 200ms;
            display: block;
        }

        span.input-header {
            font-size: 70%;
            position: absolute;
        }

        :host.floating span.input-header {
            position: relative;
        }

        :host.floating span {
            opacity: 1;
        }
    `],
    providers: [{provide: MatFormFieldControl, useExisting: DurationInputComponent}]
})
export class DurationInputComponent implements MatFormFieldControl<number>, OnDestroy, ControlValueAccessor {
    static nextId = 0;

    @HostBinding() id = `ks-duration-input-${DurationInputComponent.nextId++}`;
    @HostBinding('attr.aria-describedby') describedBy = '';
    parts: FormGroup;
    stateChanges: Subject<void> = new Subject<void>();
    focused = false;
    errorState = false;
    controlType = 'ks-duration-input';

    @Input()
    get value(): number {
        const v = this.parts.value;
        const durationInSeconds =
            (v.years * 365 * 24 * 60 * 60) +
            (v.months * 30 * 24 * 60 * 60) +
            (v.days * 24 * 60 * 60) +
            (v.hours * 60 * 60) +
            (v.minutes * 60) + v.seconds;

        return durationInSeconds;
    }

    set value(seconds: number) {
        this.parts.setValue(this.secondsToDuration(seconds));
        this.stateChanges.next();
    }

    @Input()
    get placeholder() {
        return this._placeholder;
    }

    set placeholder(plh) {
        this._placeholder = plh;
        this.stateChanges.next();
    }

    private _placeholder: string;

    @Input()
    get disabled(): boolean {
        return this._disabled;
    }

    set disabled(value: boolean) {
        this._disabled = coerceBooleanProperty(value);
        this._disabled ? this.parts.disable() : this.parts.enable();
        this.stateChanges.next();
    }

    private _disabled = false;

    @Input()
    get required() {
        return this._required;
    }

    set required(req) {
        this._required = coerceBooleanProperty(req);
        this.stateChanges.next();
    }

    private _required = false;

    get empty() {
        let v = this.parts.value;
        return !v.years && !v.months && !v.days && !v.hours && !v.minutes && !v.seconds;
    }

    @HostBinding('class.floating')
    get shouldLabelFloat() {
        return this.focused || !this.empty;
    }

    @Output() changed: EventEmitter<number> = new EventEmitter<number>();

    private _onChange: () => void = () => null;

    constructor(
        @Optional() @Self() public ngControl: NgControl,
        fb: FormBuilder,
        private fm: FocusMonitor,
        private elRef: ElementRef<HTMLElement>) {
        this.parts = fb.group({
            'years': null,
            'months': null,
            'days': null,
            'hours': null,
            'minutes': null,
            'seconds': null
        });

        fm.monitor(elRef.nativeElement, true).subscribe(origin => {
            this.focused = !!origin;
            this.stateChanges.next();
        });

        if (this.ngControl != null) {
            this.ngControl.valueAccessor = this;
        }
    }

    private onChange() {
        this.changed.emit(this.value);
        this._onChange();
    }


    private secondsToDuration(seconds: number): Duration {
        const years = seconds / (365 * 24 * 60 * 60);
        seconds = seconds % (365 * 24 * 60 * 60);
        const months = seconds / (30 * 24 * 60 * 60);
        seconds = seconds % (30 * 24 * 60 * 60);
        const days = seconds / (24 * 60 * 60);
        seconds = seconds % (24 * 60 * 60);
        const hours = seconds / (60 * 60);
        seconds = seconds % (60 * 60);
        const minutes = seconds / 60;
        seconds = seconds % 60;

        return new Duration(years, months, days, hours, minutes, seconds);
    }

    onContainerClick(event: MouseEvent) {
        if ((event.target as Element).tagName.toLowerCase() != 'input') {
            this.elRef.nativeElement.querySelector('input').focus();
        }
    }

    setDescribedByIds(ids: string[]) {
        this.describedBy = ids.join(' ');
    }

    ngOnDestroy() {
        this.stateChanges.complete();
        this.fm.stopMonitoring(this.elRef.nativeElement);
    }

    registerOnChange(fn: () => void): void {
        this._onChange = fn;
    }

    registerOnTouched(fn: any): void {
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    writeValue(seconds: number): void {
        this.value = seconds;
    }

    focus(){
        this.elRef.nativeElement.querySelector('input').focus();
    }
}