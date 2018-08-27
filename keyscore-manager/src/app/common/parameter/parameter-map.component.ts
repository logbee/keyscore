import {Component, forwardRef, Input} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

@Component({
    selector: "parameter-map",
    template:
            `
        <div class="form-row">
            <div class="form-group">
                <input #addItemInputKey class="form-control" type="text" placeholder="KEY">
            </div>
            <div class="form-group ml-1">
                <input #addItemInputValue class="form-control" type="text" placeholder="VALUE">
            </div>
            <div class="form-group ml-1">
                       <button class="btn btn-info ml-3" (click)="addItem(addItemInputKey.value,addItemInputValue.value)"><img
                        width="20em" src="/assets/images/ic_add_circle_white.svg" alt="Add"/>
                </button>
            </div>

        </div>

        <div (click)="onTouched()" class="mb-3" *ngIf="objectKeys(parameterValues).length > 0">
            <div style="display: inline-block; margin-left: 5px; margin-right: 5px;"
                 *ngFor="let key of objectKeys(parameterValues);index as i">
                <span class="badge badge-pill badge-info" style="font-size: large"><strong>{{key}} : {{parameterValues[key]}}</strong>
                    <span (click)="removeItem(key)">
                      <img class="pl-3"  alt="remove" src="/assets/images/ic_cancel_white_24px.svg"/>
                    </span>
                </span>
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
