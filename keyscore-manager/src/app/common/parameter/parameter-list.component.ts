import {Component, forwardRef, Input, OnInit, ViewChild} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {Parameter} from "../../models/parameters/Parameter";
import {ResolvedParameterDescriptor} from "../../models/parameters/ParameterDescriptor";
import {DatasetTableModel} from "../../models/dataset/DatasetTableModel";
import {BehaviorSubject, Observable} from "rxjs/index";
import {Dataset} from "../../models/dataset/Dataset";
import {AutocompleteInputComponent} from "./autocomplete-input.component";

@Component({
    selector: "parameter-list",
    template:
            `
        <div fxLayout="row" fxLayoutGap="15px">
            <auto-complete-input #addItemInput 
                                 [datasets]="datasets$ | async"
                                 [hint]="parameterDescriptor?.descriptor?.hint"
                                 [parameterDescriptor]="parameterDescriptor"
                                 [parameter]="parameter">

            </auto-complete-input>
            <button mat-icon-button color="accent" (click)="addItem(addItemInput.value)">
                <mat-icon>add_circle_outline</mat-icon>
            </button>
        </div>
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-hint *ngIf="fieldNameEmpty" style="color: rgba(204,16,8,0.65);">
                {{'PARAMETERLISTCOMPONENT.NAMEREQUIRED' | translate}}
            </mat-hint>
            <mat-hint *ngIf="duplicate" style="color: rgba(204,16,8,0.65);">
                {{'PARAMETERLISTCOMPONENT.NAMEDUPLICATE' | translate}}
            </mat-hint>
        </div>
        <div (click)="onTouched()" *ngIf="parameterValues.length > 0">
            <div style="display: inline-block; margin: 10px"
                 *ngFor="let value of parameterValues;index as i">
                <mat-chip-list class="mat-chip-list-stacked">
                    <mat-chip [color]="'accent'">
                        {{value}}
                        <mat-icon class="badge-icon cursor-pointer" (click)="removeItem(i)">close</mat-icon>
                    </mat-chip>
                </mat-chip-list>
            </div>
        </div>

    `,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => ParameterListComponent),
            multi: true
        }
    ]
})

export class ParameterListComponent implements ControlValueAccessor, OnInit {

    @Input() public disabled = false;
    @Input() public distinctValues = true;
    @Input() public parameter: Parameter;
    @Input() public parameterDescriptor: ResolvedParameterDescriptor;
    @Input() public currentDatasetModel$: Observable<DatasetTableModel>;
    @Input() public recordIndex$: Observable<number>;
    @Input('datasets') set datasets(data: Dataset[]) {
        this.datasets$.next(data);
    };

    datasets$: BehaviorSubject<Dataset[]> = new BehaviorSubject<Dataset[]>([]);
    @ViewChild(AutocompleteInputComponent) inputField;

    public parameterValues: string[] = [];
    private duplicate: boolean;
    private fieldNameEmpty: boolean;

    public onChange = (elements: string[]) => {
        undefined;
    };

    public onTouched = () => {
        undefined;
    };

    public ngOnInit(): void {
       this.parameterValues = [...(this.parameter.value as string[])];
    }

    public writeValue(elements: string[]): void {

        this.parameterValues = elements;
        this.onChange(elements);

    }

    public registerOnChange(f: (elements: string[]) => void): void {
        this.onChange = f;
    }

    public registerOnTouched(f: () => void): void {
        this.onTouched = f;
    }

    public setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    get value(): string[] {
        return [...this.parameterValues];
    }

    public removeItem(index: number) {
        const newValues = [...this.parameterValues];
        newValues.splice(index, 1);
        this.writeValue(newValues);
    }

    public addItem(value: string) {
        if (value) {
            if (!this.distinctValues || (this.distinctValues && !this.parameterValues.some((x) => x === value))) {
                this.duplicate = false;
                this.fieldNameEmpty = false;
                const newValues = [...this.parameterValues];
                newValues.push(value);
                this.writeValue(newValues);
                this.inputField.clearInput();

            } else {
                this.duplicate = true;
            }
        } else {
            this.fieldNameEmpty = true;
        }

    }
}
