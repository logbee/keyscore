import {Component, ElementRef, forwardRef, Input, OnInit, ViewChild} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {deepcopy} from "../../util";
import {Field} from "../../models/dataset/Field";
import {TextValue, ValueJsonClass} from "../../models/dataset/Value";
import {Parameter} from "../../models/parameters/Parameter";
import {BehaviorSubject} from "rxjs/index";
import {ResolvedParameterDescriptor} from "../../models/parameters/ParameterDescriptor";
import {Dataset} from "../../models/dataset/Dataset";
import {AutocompleteInputComponent} from "./autocomplete-input.component";

@Component({
    selector: "parameter-map",
    template:
            `
        <div fxLayout="row" fxLayoutGap="15px">
            <!--<mat-form-field class="half">-->
            <!--<input matInput #addItemInputKey type="text" placeholder="Key" value="" [matAutocomplete]="auto">-->
            <!--</mat-form-field>-->

            <auto-complete-input #addItemInputKey
                                 [datasets]="datasets$ | async"
                                 [hint]="parameterDescriptor?.descriptor?.hint"
                                 [parameterDescriptor]="parameterDescriptor"
                                 [labelText]="'Key'"
                                 [parameter]="parameter">

            </auto-complete-input>

            <mat-form-field>
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
    @Input() public parameterDescriptor: ResolvedParameterDescriptor;
    @Input() public parameter: Parameter;

    @Input('datasets') set datasets(data: Dataset[]) {
        this.datasets$.next(data);
    }

    private datasets$: BehaviorSubject<Dataset[]> = new BehaviorSubject<Dataset[]>([]);

    @ViewChild('addItemInputKey') inputKeyField: ElementRef;
    @ViewChild(AutocompleteInputComponent) inputKeyComponent;
    @ViewChild('addItemInputValue') inputValueField: ElementRef;

    public parameterValues: Field[];
    public keyEmpty: boolean;
    public duplicateMapping: boolean;

    public onChange = (elements: Field[]) => {
        undefined;
    };


    public onTouched = () => {
        undefined;
    };

    public writeValue(elements: Field[]): void {
        this.parameterValues = deepcopy(elements, []);
        this.onChange(elements);
    }

    public ngOnInit() {
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
                    (newValues[existingIndex].value as TextValue).value = value;
                } else {
                    this.duplicateMapping = true;
                }
            } else {
                newValues.push({name: key, value: {jsonClass: ValueJsonClass.TextValue, value: value}});
                this.inputKeyComponent.clearInput();
                this.inputValueField.nativeElement.value = '';
                this.inputKeyComponent.focus();
            }
            this.writeValue(newValues);
        } else {
            this.keyEmpty = true;
        }


    }
}
