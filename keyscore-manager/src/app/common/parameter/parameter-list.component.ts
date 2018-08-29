import {Component, forwardRef, Input} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {Observable} from "rxjs/index";
import {delay} from "rxjs/internal/operators";
import {__await} from "tslib";

@Component({
    selector: "parameter-list",
    template:
            `
        <div>
            <div>
                <input #addItemInput type="text" placeholder="NAME OF FIELD">
            </div>
            <div>
                <button (click)="addItem(addItemInput.value)"><img
                       width="20em" src="/assets/images/ic_add_circle_white.svg" alt="Add"/>
                </button>
            </div>
        </div>
        <div (click)="onTouched()" *ngIf="parameterValues.length > 0">
            <div class="custom-badge"
                 *ngFor="let value of parameterValues;index as i">
                <span class="m-2 badge badge-pill badge-info" >{{value}}
                    <span (click)="removeItem(i)">
                      <img alt="remove" src="/assets/images/ic_cancel_white_24px.svg"/>
                    </span>
                </span>
            </div>
        </div>
        <div *ngIf="this.duplicate" role="alert">
            {{'ALERT.DUPLICATE' | translate}} {{'ALERT.DUPLICATETEXT' | translate}}
        </div>
    `,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => ParameterList),
            multi: true
        }
    ]
})

export class ParameterList implements ControlValueAccessor {

    @Input() public disabled = false;
    @Input() public distinctValues = true;
    public parameterValues: string[];
    private duplicate: boolean = false;

    public onChange = (elements: string[]) => {
        return;
    };

    public onTouched = () => {
        return;
    };

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
        return this.parameterValues;
    }

    public removeItem(index: number) {
        const newValues = Object.assign([], this.parameterValues);
        newValues.splice(index, 1);
        this.writeValue(newValues);
    }

    public addItem(value: string) {

        if (!this.distinctValues || (this.distinctValues && !this.parameterValues.some((x) => x === value))) {
            const newValues = Object.assign([], this.parameterValues);
            newValues.push(value);
            this.writeValue(newValues);
        } else {
            this.duplicate = true;
            this.delay(2000).then( _ => {
                this.duplicate = false;
            })
        }

    }

    private delay (ms: number) {
        return new Promise(resolve => setTimeout(resolve, ms))
    }
}
