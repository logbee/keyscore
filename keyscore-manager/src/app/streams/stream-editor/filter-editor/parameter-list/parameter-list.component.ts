import {Component, forwardRef, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {Parameter, TextListParameter} from "../../../streams.model";
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
                    <button class="btn btn-danger d-inline-block " (click)="removeItem(i)">Delete</button>
                </li>
            </div>

        </div>
        <div class="row mt-2 pl-3 pr-3 justify-content-between ">
            <input #addItemInput class="form-control col-11" type="text">

            <button class="btn btn-success col-1" (click)="addItem(addItemInput.value)">Add
            </button>

        </div>`,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => ParameterList),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting:forwardRef(()=>ParameterList),
            multi: true
        }
    ]
})

export class ParameterList implements OnInit, ControlValueAccessor,OnChanges {

    @Input() parameter: TextListParameter;
    @Input() disabled = false;

    values$:Observable<string[]>;

    parameterValues: string[];

    validateFn:Function;

    /*@HostBinding('style.opacity')
    get opacity():number {
        return this.disabled ? 0.25 : 1;
    }*/


    onChange = (elements: string[]) => {
    };

    onTouched = () => {
    };

    validate(c:FormControl){
        return this.validateFn(c);
    }

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
        this.values$=Observable.of(this.parameterValues);
        this.validateFn=createElementRangeValidator(this.parameter.min,this.parameter.max);

    }

    ngOnChanges(changes:SimpleChanges){
        if(changes.parameter){
            this.validateFn = createElementRangeValidator(this.parameter.min,this.parameter.max);
        }
    }

    removeItem(index: number) {
        this.parameter.value.splice(index, 1);
        this.writeValue(this.parameter.value);
    }

    addItem(value: string) {
        this.parameter.value.push(value);
    }
}

export function createElementRangeValidator(minValue: number, maxValue: number) {
    return function validateElementRange(c: FormControl) {
        let err = {
            outOfRangeError: {
                given: c.value.length,
                max: maxValue,
                min: minValue
            }
        };
        return (c.value.length > +maxValue || c.value.length < +minValue) ? err : null;
    }
}