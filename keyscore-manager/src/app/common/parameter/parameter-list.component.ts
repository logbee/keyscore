import {Component, forwardRef, Input, OnInit} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {Observable} from "rxjs/index";
import {delay} from "rxjs/internal/operators";
import {__await} from "tslib";
import {Parameter} from "../../models/parameters/Parameter";

@Component({
    selector: "parameter-list",
    template:
            `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field>
                <input #addItemInput matInput type="text" placeholder="Name of Field">
            </mat-form-field>
            <button mat-icon-button color="accent" (click)="addItem(addItemInput.value)">
                <mat-icon>add_circle_outline</mat-icon>
            </button>
        </div>
        <div (click)="onTouched()" *ngIf="parameterValues.length > 0">
            <div style="display: inline-block; margin: 10px"
                 *ngFor="let value of parameterValues;index as i">
                <mat-chip-list class="mat-chip-list-stacked">
                    <mat-chip [color]="'accent'">
                        {{value}}
                        <mat-icon class="badge-icon cursor-pointer" (click)="removeItem(i)">close</mat-icon>
                    </mat-chip>
                </mat-chip-list>
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

export class ParameterList implements ControlValueAccessor,OnInit {

    @Input() public disabled = false;
    @Input() public distinctValues = true;
    @Input() public parameter: Parameter;
    public parameterValues: string[];
    private duplicate: boolean = false;

    public onChange = (elements: string[]) => {
        return;
    };

    public onTouched = () => {
        return;
    };

    public ngOnInit(): void{
        this.parameterValues = this.parameter.value;
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
