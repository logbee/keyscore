import {Component, forwardRef, Input} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

@Component({
    selector: 'parameter-map',
    template:
            `        
        <div class="form-row mt-2 pl-1">
            <div class="form-group">
                <input #addItemInputKey class="form-control" type="text" placeholder="Field Name">
            </div>
            <div class="form-group ml-1">
                <input #addItemInputValue class="form-control" type="text" placeholder="Field Value">
            </div>
            <div class="form-group ml-1">
                <button class="btn btn-success"
                        (click)="addItem(addItemInputKey.value,addItemInputValue.value)"><img
                        src="/assets/images/ic_add_white_24px.svg" alt="Remove"/>
                </button>
            </div>

        </div>

        <div class="card" (click)="onTouched()" *ngIf="objectKeys(parameterValues).length > 0">
            <div class="list-group-flush col-12">
                <li class="list-group-item d-flex justify-content-between"
                    *ngFor="let key of objectKeys(parameterValues);index as i">
                    <span class="align-self-center">{{key}} : {{parameterValues[key]}}</span>
                    <button class="btn btn-danger d-inline-block " (click)="removeItem(key)"><img
                            src="/assets/images/ic_delete_white_24px.svg" alt="Remove"/></button>
                </li>
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

    @Input() disabled = false;

    objectKeys = Object.keys;
    parameterValues: Map<string, string>;

    /*@HostBinding('style.opacity')
    get opacity():number {
        return this.disabled ? 0.25 : 1;
    }*/


    onChange = (elements: Map<string, string>) => {
    };

    onTouched = () => {

    };


    writeValue(elements: Map<string, string>): void {

        this.parameterValues = Object.assign({}, elements);
        console.log('parameterValues: ' + JSON.stringify(this.parameterValues));
        this.onChange(elements);

    }

    registerOnChange(f: (elements: Map<string, string>) => void): void {
        this.onChange = f;
    }

    registerOnTouched(f: () => void): void {
        this.onTouched = f;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    get value(): Map<string, string> {
        return this.parameterValues;

    }

    removeItem(key: string) {
        let newValues = Object.assign({}, this.parameterValues);
        delete newValues[key];
        this.writeValue(newValues);
    }

    addItem(key: string, value: string) {
        let newValues = Object.assign({}, this.parameterValues);
        newValues[key] = value;
        this.writeValue(newValues);

    }
}
