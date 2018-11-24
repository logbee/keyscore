import {Component, forwardRef, Input, OnInit} from "@angular/core";
import {ControlValueAccessor, FormBuilder, FormGroup, NG_VALUE_ACCESSOR} from "@angular/forms";
import {Parameter} from "../../models/parameters/Parameter";
import {ResolvedParameterDescriptor} from "../../models/parameters/ParameterDescriptor";
import {Dataset} from "../../models/dataset/Dataset";
import * as _ from "lodash"
import {BehaviorSubject} from "rxjs/index";

@Component({
    selector: 'field-name-input',
    template: `
        <mat-form-field [formGroup]="group">
            <input matInput type="text" formControlName="fieldName" [id]="parameter.ref.id"
                   [placeholder]="parameterDescriptor.defaultValue" [matAutocomplete]="auto">
            <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
            <mat-autocomplete #auto="matAutocomplete">
                <mat-option *ngFor="let field of hints" [value]="field">{{field}}</mat-option>
            </mat-autocomplete>
        </mat-form-field>

    `,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => FieldNameInputComponent),
            multi: true
        }
    ]
})

export class FieldNameInputComponent implements OnInit, ControlValueAccessor {
    group: FormGroup;

    private _disabled = false;

    private _fieldName: string = "";

    private hints: string[] = [];

    @Input() hint: string;
    @Input() parameter: Parameter;
    @Input() parameterDescriptor: ResolvedParameterDescriptor;

    @Input('datasets') set datasets(data: Dataset[]) {
        this.datasets$.next(data);
    };

    datasets$: BehaviorSubject<Dataset[]> = new BehaviorSubject<Dataset[]>([]);


    get value(): string | null {
        return this._fieldName;
    };

    constructor(fb: FormBuilder) {
        this.group = fb.group({
            'fieldName': ''
        });

        this.group.valueChanges.subscribe(values => this.writeValue(values['fieldName']));
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
        console.log("Write Value: ", fieldName);
        this._fieldName = fieldName;
        this.onChange(fieldName);
    }

    ngOnInit(): void {
        this.group.setValue({'fieldName': this.parameter.value});
        this.writeValue(this.parameter.value);
        console.log("TEST : ", this.parameterDescriptor);
        this.datasets$.subscribe(datasets => {
            console.log("TEST datasets: ", this.datasets);
            if (this.hint === "PresentField") {
                let fieldNames = _.flatten(_.flatten(datasets.map(dataset => dataset.records.map(record => record.fields.map(field => field.name)))));
                this.hints = Array.from(new Set(fieldNames));
            }
        });
    }
}