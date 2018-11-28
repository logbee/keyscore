import {Component, ElementRef, forwardRef, Input, OnInit, ViewChild} from "@angular/core";
import {Observable} from "rxjs/internal/Observable";
import {DatasetTableModel} from "../../models/dataset/DatasetTableModel";
import {
    FieldNameListParameterDescriptor, FieldParameterDescriptor,
    ResolvedParameterDescriptor
} from "../../models/parameters/ParameterDescriptor";
import {Parameter} from "../../models/parameters/Parameter";
import {Field} from "../../models/dataset/Field";
import {ValueJsonClass} from "../../models/dataset/Value";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

@Component({
    selector: 'field-name-parameter',
    template: `
        <mat-form-field>
            <input matInput #addItemRef type="text" placeholder="{{'PARAMETERLISTCOMPONENT.NAMEOFFIELD' | translate}}" [matAutocomplete]="auto"/>
        </mat-form-field>

        <button mat-icon-button (click)="addValue(addItemRef.value)">favorite</button>
        
        <!--Autocompletion-->
        <mat-autocomplete #auto="matAutocomplete">
            <mat-option *ngFor="let field of hints" [value]="field">{{field}}</mat-option>
        </mat-autocomplete>
    `,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => FieldNameParameterComponent),
            multi: true
        }
    ]
})
export class FieldNameParameterComponent implements ControlValueAccessor, OnInit {
    @Input() public disabled = false;
    @Input() public currentDatasetModel$: Observable<DatasetTableModel>;
    @Input() public recordIndex$: Observable<number>;
    @Input() public parameterDescriptor: ResolvedParameterDescriptor;
    @Input() public parameter: Parameter;
    private hints: string[] = [];
    private hint = undefined;
    public recordIndex: number;
    @ViewChild('addItemRef') inputField: ElementRef;

    public parameterValue: string;

    public onChange = (element: Field) => {
        console.log("TEST element: ", element);
        return;
    };

    public onTouched = () => {
        return;
    };

    public writeValue(element: Field):void {
        this.parameterValue = this.parameter.value;
        console.log("TEST element: ", element);
        this.onChange(element)
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
        console.log("TEST Initial value" ,this.parameter.value);
        this.parameterValue = this.parameter.value;
        this.inputField.nativeElement.value = this.parameterValue;
    }

    public registerOnChange(f:(elements: Field) => void):void {
        this.onChange = f;
    }

    public registerOnTouched(f: () => void):void  {
        this.onTouched = f;
    }

    public setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    get value(): string {
        return this.parameterValue;
    }

    public addValue(value: string) {
        console.log("TEST " ,value);
        this.writeValue({name: this.parameterDescriptor.info.displayName, value: {jsonClass: ValueJsonClass.TextValue, value: value}})

    }
}