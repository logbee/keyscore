import {Component, forwardRef, Input, OnInit, ViewChild} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {Parameter, ResolvedParameterDescriptor,FieldNamePatternParameter, DatasetTableModel, Dataset, PatternType, PatternTypeToString} from "keyscore-manager-models";
import {BehaviorSubject, Observable} from "rxjs";
import {AutocompleteInputComponent} from "./autocomplete-input.component";

@Component({
    selector: "parameter-fieldnamepattern",
    template:
            `
        <div fxLayout="row" fxLayoutGap="15px">
            <auto-complete-input #addItemInput fxFlex="80"
                                 [datasets]="datasets$ | async"
                                 [hint]="parameterDescriptor?.descriptor?.hint"
                                 [parameterDescriptor]="parameterDescriptor"
                                 [parameter]="parameter"
                                 (onChangeEmit)="writeValue({fieldName:addItemInput.value,patternType:patternSelect.value})">

            </auto-complete-input>
            <mat-form-field fxFlex>
                <mat-label>Pattern Type</mat-label>
                <mat-select #patternSelect (selectionChange)="writeValue({fieldName:addItemInput.value,patternType:patternSelect.value})">
                    <mat-option *ngFor="let choice of parameterDescriptor.supports" [value]="choice">
                        {{patternToString(choice)}}
                    </mat-option>
                </mat-select>
            </mat-form-field>
        </div>
    `,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => ParameterFieldnamepatternComponent),
            multi: true
        }
    ]
})

export class ParameterFieldnamepatternComponent implements ControlValueAccessor, OnInit {

    @Input() public disabled = false;
    @Input() public parameter: FieldNamePatternParameter;
    @Input() public parameterDescriptor: ResolvedParameterDescriptor;

    @Input('datasets') set datasets(data: Dataset[]) {
        this.datasets$.next(data);
    };

    datasets$: BehaviorSubject<Dataset[]> = new BehaviorSubject<Dataset[]>([]);
    @ViewChild(AutocompleteInputComponent) inputField;

    public parameterValue: { fieldName: string, patternType: PatternType } = {fieldName: "", patternType: 0};

    public onChange = (value: string, patternType: PatternType) => {
        undefined;
    };

    public onTouched = () => {
        undefined;
    };

    public ngOnInit(): void {
        console.log(JSON.stringify(this.parameter));
        this.parameterValue = {fieldName:this.parameter.value,patternType:this.parameter.patternType};
    }

    public writeValue(value:{fieldName: string, patternType: PatternType}): void {

        this.parameterValue = {fieldName: value.fieldName || "", patternType: value.patternType || 0};
        console.log(JSON.stringify(this.parameterValue));
        this.onChange(value.fieldName, value.patternType);

    }

    public registerOnChange(f: (value: string, patternType: PatternType) => void): void {
        this.onChange = f;
    }

    public registerOnTouched(f: () => void): void {
        this.onTouched = f;
    }

    public setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    get value(): { fieldName: string, patternType: PatternType } {
        return this.parameterValue;
    }

    private patternToString = PatternTypeToString;

}
