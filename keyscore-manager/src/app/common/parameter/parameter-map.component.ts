import {Component, forwardRef, Input, OnInit} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {deepcopy} from "../../util";
import {Field} from "../../models/dataset/Field";
import {TextValue, ValueJsonClass} from "../../models/dataset/Value";
import {Parameter} from "../../models/parameters/Parameter";

@Component({
    selector: "parameter-map",
    template:
            `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field class="half">
                <input matInput #addItemInputKey type="text" placeholder="Key">
            </mat-form-field>
            <mat-form-field class="half">
                <input matInput #addItemInputValue type="text" placeholder="Value">
            </mat-form-field>
            <button mat-icon-button color="accent" (click)="addItem(addItemInputKey.value,addItemInputValue.value)">
                <mat-icon>add_circle_outline</mat-icon>
            </button>
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

export class ParameterMap implements ControlValueAccessor,OnInit {

    @Input() public disabled = false;
    @Input() public parameter: Parameter;

    public parameterValues: Field[];

    public onChange = (elements: Field[]) => {
        return;
    };


    public onTouched = () => {
        return;
    };

    public writeValue(elements: Field[]): void {
        this.parameterValues = deepcopy(elements,[]);
        this.onChange(elements);
    }

    public ngOnInit(){
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
            let newValues: Field[] = deepcopy(this.parameterValues,[]);
            newValues.splice(removeIndex, 1);
            this.writeValue(newValues);
        }
    }

    public addItem(key: string, value: string) {
        const newValues: Field[] = deepcopy(this.parameterValues,[]);
        let existingIndex = newValues.findIndex(field => field.name === key);
        if (existingIndex >= 0) {
            (newValues[existingIndex].value as TextValue).value = value;
        } else {
            newValues.push({name: key, value: {jsonClass: ValueJsonClass.TextValue, value: value}});
        }
        this.writeValue(newValues);

    }
}
