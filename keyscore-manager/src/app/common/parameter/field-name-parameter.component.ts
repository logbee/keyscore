import {Component, ElementRef, forwardRef, HostBinding, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {Observable} from "rxjs/internal/Observable";
import {DatasetTableModel} from "../../models/dataset/DatasetTableModel";
import {
    FieldNameListParameterDescriptor, FieldParameterDescriptor,
    ResolvedParameterDescriptor
} from "../../models/parameters/ParameterDescriptor";
import {Parameter} from "../../models/parameters/Parameter";
import {Field} from "../../models/dataset/Field";
import {Value, ValueJsonClass} from "../../models/dataset/Value";
import {ControlValueAccessor, FormBuilder, NG_VALUE_ACCESSOR, NgControl} from "@angular/forms";
import {MatFormFieldControl} from "@angular/material";
import {Subject} from "rxjs/index";
import {FocusMonitor} from "@angular/cdk/a11y";
import {coerceBooleanProperty} from "@angular/cdk/typings/esm5/coercion";

@Component({
    selector: 'field-name-parameter',
    template: `
 
        <input #addItemRef type="text" placeholder="{{'PARAMETERLISTCOMPONENT.NAMEOFFIELD' | translate}}" [matAutocomplete]="auto"/>
        <!--Autocompletion-->
        <mat-autocomplete #auto="matAutocomplete">
            <mat-option *ngFor="let field of hints" [value]="field">{{field}}</mat-option>
        </mat-autocomplete>
    `,
    providers: [
        {
            provide: MatFormFieldControl,
            useExisting: FieldNameParameterComponent
        }
    ]
})
export class FieldNameParameterComponent implements MatFormFieldControl<string>, OnDestroy{
    @Input() public currentDatasetModel$: Observable<DatasetTableModel>;
    @Input() public recordIndex$: Observable<number>;
    @Input() public parameterDescriptor: ResolvedParameterDescriptor;

    @Input()
    get required() {
        return this._required;
    }

    set required(req) {
        this._required = coerceBooleanProperty(req);
        this.stateCanges.next();
    }
    @Input()
    get placeholder() {
        return this._placeholder;
    }

    set placeholder(placeholder) {
        this._placeholder = placeholder;
        this.stateCanges.next();
    }

    private _placeholder: string;
    static nextId = 0;
    focused = false;
    ngControl: NgControl = null;
    private _required = false;
    errorState = false;
    controlType = 'field-name-input';
    stateCanges = new Subject<void>();
    private hints: string[] = [];
    private hint = undefined;
    public recordIndex: number;

    @HostBinding('class.floating')
    get shouldLabelFloat() {
        return this.focused || !this.empty;
    }

    @HostBinding() id = `field-input-${FieldNameParameterComponent.nextId++}`;
    @ViewChild('addItemRef') inputField: ElementRef;


    set value(field: string | null) {
        this.stateCanges.next();
    }

    get empty() {
        return !(this.value && this.value.length > 0);
    }

    onContainerClick(event: MouseEvent) {
        if ((event.target as Element).tagName.toLowerCase() != 'input') {
            this.elementRef.nativeElement.querySelector('input').focus();
        }
    }

    constructor(fb: FormBuilder, private fm: FocusMonitor, private elementRef: ElementRef<HTMLElement>) {
        fm.monitor(elementRef.nativeElement, true).subscribe(origin => {
            this.focused = !!origin;
            this.stateCanges.next();
        });
    }

    ngOnInit() {
        this.recordIndex$.subscribe(recordindex => {
            this.recordIndex = recordindex;
        });

        this.hint = (this.parameterDescriptor as FieldParameterDescriptor).hint;

        this.currentDatasetModel$.subscribe(currentDatasetModel => {
            if (currentDatasetModel != undefined) {
                if (this.hint === "PresentField") {
                    this.hints = currentDatasetModel.records[this.recordIndex].rows.map(row => row.input.name);
                } else {
                    console.log("TEST Nothing to do !")
                }
            }
        });
    }

    ngOnDestroy() {
        this.stateCanges.complete();
        this.fm.stopMonitoring(this.elementRef.nativeElement);
    }
}