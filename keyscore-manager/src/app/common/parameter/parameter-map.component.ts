import {Component, ElementRef, forwardRef, Input, OnInit, ViewChild} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {deepcopy} from "../../util";
import {Field} from "../../models/dataset/Field";
import {TextValue, ValueJsonClass} from "../../models/dataset/Value";
import {Parameter} from "../../models/parameters/Parameter";
import {Observable} from "rxjs/index";
import {DatasetTableModel} from "../../models/dataset/DatasetTableModel";
import {
    FieldNameListParameterDescriptor,
    ResolvedParameterDescriptor
} from "../../models/parameters/ParameterDescriptor";

@Component({
    selector: "parameter-map",
    template:
            `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field class="half">
                <input matInput #addItemInputKey type="text" placeholder="Key" value="" [matAutocomplete]="auto">
            </mat-form-field>

            <mat-form-field class="half">
                <input matInput #addItemInputValue type="text" placeholder="Value" value="">
            </mat-form-field>
            <button mat-icon-button color="accent" (click)="addItem(addItemInputKey.value,addItemInputValue.value)">
                <mat-icon>add_circle_outline</mat-icon>
            </button>
        </div>

        <div fxLayout="row" fxLayoutGap="15px">
            <mat-hint *ngIf="keyEmpty" style="color: rgba(204,16,8,0.65);">
                {{'PARAMETERMAPCOMPONENT.KEYREQUIRED' | translate}}
            </mat-hint>
            <mat-hint *ngIf="duplicateMapping" style="color: rgba(204,16,8,0.65);">
                {{'PARAMETERMAPCOMPONENT.DUPLICATE' | translate}}
            </mat-hint>
        </div>

        <div (click)="onTouched()" *ngIf="parameterValues.length > 0">
            <div style="display: inline-block; margin: 10px;"
                 *ngFor="let field of parameterValues">
                <mat-chip-list class="mat-chip-list-stacked">
                    <mat-chip>{{field.name}} : {{field.value.value}}
                        <mat-icon (click)="removeItem(field)">close</mat-icon>
                    </mat-chip>
                </mat-chip-list>
            </div>
        </div>
        
        <!--Autocompletion-->
        <mat-autocomplete #auto="matAutocomplete">
            <mat-option *ngFor="let field of hints" [value]="field">{{field}}</mat-option>
        </mat-autocomplete>
    `,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => ParameterMap),
            multi: true
        }
    ]
})

export class ParameterMap implements ControlValueAccessor, OnInit {

    @Input() public disabled = false;
    @Input() public descriptor: ResolvedParameterDescriptor;
    @Input() public parameter: Parameter;

    @ViewChild('addItemInputKey') inputKeyField: ElementRef;
    @ViewChild('addItemInputValue') inputValueField: ElementRef;

    public parameterValues: Field[];
    public keyEmpty: boolean;
    public duplicateMapping: boolean;
    private hints: string[] = [];
    private hint = undefined;
    private recordIndex: number;
    @Input() public currentDatasetModel$: Observable<DatasetTableModel>;
    @Input() public recordIndex$: Observable<number>;
    public onChange = (elements: Field[]) => {
        return;
    };


    public onTouched = () => {
        return;
    };

    public writeValue(elements: Field[]): void {
        this.parameterValues = deepcopy(elements, []);
        this.onChange(elements);
    }

    public ngOnInit() {
        this.recordIndex$.subscribe(recordindex => {
            this.recordIndex = recordindex;
        });

        this.hint = (this.descriptor as FieldNameListParameterDescriptor).descriptor.hint;

        this.currentDatasetModel$.subscribe(currentDatasetModel => {
            if (currentDatasetModel != undefined) {
                if (this.hint === "PresentField") {
                    this.hints = currentDatasetModel.records[this.recordIndex].rows.map(row => row.input.name);
                } else {
                    console.log("Nothing to do !")
                }
            }
        });
        this.parameterValues = this.parameter.value;
    }

    public registerOnChange(f: (elements: Field[]) => void): void {
        this.onChange = f;
    }

    public registerOnTouched(f: () => void): void {
        this.onTouched = f;
    }

    public setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    get value(): Field[] {
        return this.parameterValues;

    }

    public removeItem(toRemove: Field) {
        this.hints.push(toRemove.name);
        let removeIndex = this.parameterValues.findIndex(field => field.name === toRemove.name);
        if (removeIndex >= 0) {
            let newValues: Field[] = deepcopy(this.parameterValues, []);
            newValues.splice(removeIndex, 1);
            this.writeValue(newValues);
        }
    }

    public addItem(key: string, value: string) {
        if (key) {
            this.keyEmpty = false;
            this.duplicateMapping = false;
            const newValues: Field[] = deepcopy(this.parameterValues, []);
            let existingIndex = newValues.findIndex(field => field.name === key);
            if (existingIndex >= 0) {
                let currentVal = (newValues[existingIndex].value as TextValue).value;
                if (currentVal !== value) {
                    this.duplicateMapping = false;
                    this.hints = this.hints.filter(hint => hint !== value);
                    (newValues[existingIndex].value as TextValue).value = value;
                } else {
                    this.duplicateMapping = true;
                }
            } else {
                newValues.push({name: key, value: {jsonClass: ValueJsonClass.TextValue, value: value}});
                this.inputKeyField.nativeElement.value='';
                this.inputValueField.nativeElement.value='';
                this.inputKeyField.nativeElement.focus();
            }
            this.writeValue(newValues);
        } else {
            this.keyEmpty = true;
        }


    }
}
