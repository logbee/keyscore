import {Component, forwardRef, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {Parameter, ParameterDescriptor, TextListParameter} from "../../../streams.model";
import {Observable} from "rxjs/Observable";
import {ControlValueAccessor, FormControl, NG_VALIDATORS, NG_VALUE_ACCESSOR} from "@angular/forms";
import {HostBinding} from "@angular/compiler/src/core";

@Component({
    selector: 'parameter-list',
    template:
            `
        <div class="card" (click)="onTouched()">
            <div class="list-group-flush col-12">
                <li class="list-group-item d-flex justify-content-between"
                    *ngFor="let value of values$ | async;index as i">
                    <span class="align-self-center">{{value}}</span>
                    <button class="btn btn-danger d-inline-block " (click)="removeItem(i)"><img
                            src="/assets/images/ic_delete_white_24px.svg" alt="Remove"/></button>
                </li>
            </div>

        </div>
        <div class="form-row mt-2 pl-1">
            <div class="form-group">
                <input #addItemInput class="form-control" type="text">
            </div>
            <div class="form-group ml-1">
                <button class="btn btn-success" (click)="addItem(addItemInput.value)"><img
                        src="/assets/images/ic_add_white_24px.svg" alt="Remove"/>
                </button>
            </div>

        </div>`,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => ParameterList),
            multi: true
        }
    ]
})

export class ParameterList implements OnInit, ControlValueAccessor {

    @Input() parameter: ParameterDescriptor;
    @Input() disabled = false;
    @Input() distinctValues = true;

    values$: Observable<string[]>;

    parameterValues: string[];

    /*@HostBinding('style.opacity')
    get opacity():number {
        return this.disabled ? 0.25 : 1;
    }*/


    onChange = (elements: string[]) => {
    };

    onTouched = () => {

    };


    writeValue(elements: string[]): void {
        this.parameterValues = elements;
        this.onChange(elements);
    }

    registerOnChange(f: (elements: string[]) => void): void {
        this.onChange = f;
    }

    registerOnTouched(f: () => void): void {
        this.onTouched = f;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    get value(): string[] {
        return this.parameterValues;
    }

    ngOnInit() {
        console.log(this.parameter);
        this.parameterValues = this.parameter.value;
        this.values$ = Observable.of(this.parameterValues);

    }


    removeItem(index: number) {
        this.parameter.value.splice(index, 1);
        this.writeValue(this.parameter.value);
    }

    addItem(value: string) {
        console.log('paramterValues:::' + this.parameterValues);
        if (!this.distinctValues || (this.distinctValues && !this.parameterValues.some(x => x === value))) {
            this.parameter.value.push(value);
            this.writeValue(this.parameter.value);
        }


    }
}
