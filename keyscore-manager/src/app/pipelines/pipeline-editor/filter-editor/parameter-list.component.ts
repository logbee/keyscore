import {Component, forwardRef, Input} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

@Component({
    selector: "parameter-list",
    template:
            `
        <div class="form-row mt-2 pl-1">
            <div class="form-group">
                <input #addItemInput class="form-control" type="text">
            </div>
            <div class="form-group ml-1">
                <button class="btn btn-success" (click)="addItem(addItemInput.value)"><img
                        src="/assets/images/ic_add_white_24px.svg" alt="Remove"/>
                </button>
            </div>

        </div>

        <div class="card" (click)="onTouched()" *ngIf="parameterValues.length > 0">
            <div class="list-group-flush col-12">
                <li class="list-group-item d-flex justify-content-between"
                    *ngFor="let value of parameterValues;index as i">
                    <span class="align-self-center">{{value}}</span>
                    <button class="btn btn-danger d-inline-block " (click)="removeItem(i)"><img
                            src="/assets/images/ic_delete_white_24px.svg" alt="Remove"/></button>
                </li>
            </div>

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

    public onChange = (elements: string[]) => {
        return;
    }

    public onTouched = () => {
        return;
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
        }

    }
}
