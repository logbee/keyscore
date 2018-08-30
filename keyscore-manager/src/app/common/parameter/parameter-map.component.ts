import {Component, forwardRef, Input} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

@Component({
    selector: "parameter-map",
    template:
            `
        <div class="form-row">
            <div>
                <mat-form-field>
                    <input matInput #addItemInputKey  type="text" placeholder="Key">
                </mat-form-field>    
            </div>
            <div>
                <mat-form-field>
                    <input matInput #addItemInputValue type="text" placeholder="Value">
                </mat-form-field>
            </div>
            <div>
                <button mat-raised-button color="accent" (click)="addItem(addItemInputKey.value,addItemInputValue.value)"><img
                        width="20em" src="/assets/images/ic_add_circle_white.svg" alt="Add"/>
                </button>
            </div>
        </div>

        <div (click)="onTouched()" *ngIf="objectKeys(parameterValues).length > 0">
            <div style="display: inline-block; margin: 10px;"
                 *ngFor="let key of objectKeys(parameterValues);index as i">
                <mat-chip-list class="mat-chip-list-stacked">
                    <mat-chip color="primary" selected>{{key}} : {{parameterValues[key]}}
                        <mat-icon (click)="removeItem(key)" >close</mat-icon>
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

export class ParameterMap implements ControlValueAccessor {

    @Input() public disabled = false;

    public objectKeys = Object.keys;
    public parameterValues: Map<string, string>;

    public onChange = (elements: Map<string, string>) => {
        return;
    };


    public onTouched = () => {
        return;
    };

    public writeValue(elements: Map<string, string>): void {
        this.parameterValues = Object.assign({}, elements);
        this.onChange(elements);
    }

    public registerOnChange(f: (elements: Map<string, string>) => void): void {
        this.onChange = f;
    }

    public registerOnTouched(f: () => void): void {
        this.onTouched = f;
    }

    public setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    get value(): Map<string, string> {
        return this.parameterValues;

    }

    public removeItem(key: string) {
        const newValues = Object.assign({}, this.parameterValues);
        delete newValues[key];
        this.writeValue(newValues);
    }

    public addItem(key: string, value: string) {
        const newValues = Object.assign({}, this.parameterValues);
        newValues[key] = value;
        this.writeValue(newValues);

    }
}
