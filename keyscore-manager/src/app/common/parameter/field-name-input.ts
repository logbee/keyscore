import {Component, forwardRef, HostBinding, Input, OnDestroy, Optional, Self, ElementRef, OnInit} from "@angular/core";
import {MatFormFieldControl} from "@angular/material";
import {ControlValueAccessor, NG_VALUE_ACCESSOR, FormBuilder, FormControl, FormGroup, NgControl} from "@angular/forms";
import {Parameter} from "../../models/parameters/Parameter";
import {Observable} from "rxjs/internal/Observable";
import {Subject} from "rxjs/index";
import {FocusMonitor} from "@angular/cdk/a11y";
import {coerceBooleanProperty} from "@angular/cdk/coercion";
import {ResolvedParameterDescriptor} from "../../models/parameters/ParameterDescriptor";
import {FieldParameterList} from "./field-parameter-list.component";

@Component({
    selector: 'field-name-input',
    template: `
        <mat-form-field [formGroup]="group">
            <input matInput formControlName="fieldName" type="text" [placeholder]="placeholder"
                   [autocomplete]="auto">
            <mat-autocomplete #auto="matAutocomplete">
                <mat-option *ngFor="let field of hints" [value]="field">{{field}}</mat-option>
            </mat-autocomplete>
            <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
            <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                <mat-icon>close</mat-icon>
            </button>
        </mat-form-field>

    `,
    providers: [
        {
            provide: MatFormFieldControl,
            useExisting: FieldNameInputComponent
        }
    ]
})

export class FieldNameInputComponent implements OnInit,OnDestroy, ControlValueAccessor,MatFormFieldControl<string> {
    static nextId = 0;
    @HostBinding()
    readonly id: string = `field-name-input-${FieldNameInputComponent.nextId++}`;
    @HostBinding('class.floating')
    get shouldLabelFloat(){
        return this.focused || !this.empty;
    }
    @HostBinding('attr.aria-describedby') describedBy = '';

    setDescribedByIds(ids: string[]) {
        this.describedBy = ids.join(' ');
    }


    group: FormGroup;
    focused: boolean = false;
    readonly autofilled: boolean;
    readonly controlType: string = 'field-name-input';
    readonly errorState: boolean = false;
    readonly stateChanges: Subject<void> = new Subject();

    @Input() get placeholder() {
        return this._placeholder;
    }

    set placeholder(plh: string) {
        this._placeholder = plh;
        this.stateChanges.next();
    }

    private _placeholder: string;
    @Input()
    get required() {
        return this._required;
    }
    set required(req) {
        this._required = coerceBooleanProperty(req);
        this.stateChanges.next();
    }
    private _required = false;

    @Input()
    get disabled() {
        return this._disabled;
    }
    set disabled(dis) {
        this.setDisabledState(coerceBooleanProperty(dis));
        this.stateChanges.next();
    }
    private _disabled = false;

    @Input() parameter: Parameter;
    @Input() parameterDescriptor:ResolvedParameterDescriptor;

    @Input()
    get value(): string | null {
        return this.group.controls['fieldName'].value;
    };

    set value(fieldName: string | null) {
        this.writeValue(fieldName);
        this.stateChanges.next();
    }

    get empty(){
        return !this.group.value;
    }

    constructor(fb: FormBuilder, private fm: FocusMonitor, private elemRef: ElementRef<HTMLElement>, @Optional() @Self() public ngControl: NgControl) {
        this.ngControl.valueAccessor = this;
        this.group = fb.group({
            'fieldName': ''
        });
        if (this.ngControl != null) {
            this.ngControl.valueAccessor = this;
        }

        fm.monitor(elemRef.nativeElement, true).subscribe(origin =>{
            this.focused = !!origin;
            this.stateChanges.next();
        })
    }

    onContainerClick(event: MouseEvent) {
        if ((event.target as Element).tagName.toLowerCase() != 'input') {
            this.elemRef.nativeElement.querySelector('input').focus();
        }
    }

    ngOnDestroy(): void {
        this.stateChanges.complete();
        this.fm.stopMonitoring(this.elemRef.nativeElement);
    }

    public registerOnChange(fn: (fieldName: string) => void): void {
        this.onChange = fn;
    }

    public registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this._disabled = isDisabled;
    }

    public onChange = (fieldName: string) => {
        return;
    };

    public onTouched = () => {
        return;
    };

    writeValue(fieldName: string): void {
        this.group.setValue({'fieldName': fieldName});
        this.onChange(fieldName);
    }

    ngOnInit(): void {
        this.writeValue(this.parameter.value);
    }


}