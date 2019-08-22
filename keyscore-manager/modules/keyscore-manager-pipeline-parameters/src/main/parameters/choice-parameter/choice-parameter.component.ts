import {Component, ElementRef, ViewChild} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {ChoiceParameter, ChoiceParameterDescriptor} from "./choice-parameter.model";
import {FormControl} from "@angular/forms";
import {Subscription} from "rxjs";
import {animate, style, transition, trigger} from "@angular/animations";
import * as _ from 'lodash'

@Component({
    selector: 'parameter-choice',
    template: `
        <mat-form-field>
            <mat-select [formControl]="_selectFormControl" [multiple]="descriptor.max > 1 ? '' :null"
                        (selectionChange)="selectionChanged()">
                <mat-option *ngFor="let choice of descriptor.choices;let i=index" [value]="choice.name"
                            [matTooltip]="choice.description ||null"
                            matTooltipPosition="before">
                        <span>{{choice.displayName}}</span>

                        <div @max-warn *ngIf="_showMaxError[i]" class="choice-max-error">{{descriptor.displayName}}
                            only
                            allows {{descriptor.max}} selections.
                        </div>
                </mat-option>
            </mat-select>
            <mat-label>{{descriptor.displayName}}</mat-label>
        </mat-form-field>
        <p class="parameter-warn" *ngIf=" descriptor.min > 1 && _selectFormControl.value.length < descriptor.min">
            {{descriptor.displayName}} expects at least {{descriptor.min}} selections.</p>
    `,
    animations: [
        trigger('max-warn', [
            transition(':enter', [
                style({transform: 'translateY(-100%)'}),
                animate('400ms ease-in', style({transform: 'translateY(0%)'}))
            ]),
            transition(':leave', [
                animate('400ms ease-out', style({transform: 'translateY(-100%)'}))
            ])
        ])
    ]
})
export class ChoiceParameterComponent extends ParameterComponent<ChoiceParameterDescriptor, ChoiceParameter> {
    get value(): ChoiceParameter {
        return new ChoiceParameter(this.descriptor.ref, this._selectFormControl.value)
    }

    private _selectFormControl: FormControl;


    private _selections: string[];
    private _showMaxError: boolean[] = [];

    protected onInit() {
        this._selectFormControl = new FormControl(this.parameter.value || "");
        this.descriptor.choices.forEach(_ => this._showMaxError.push(false));
    }

    private onChange() {
        this.emit(this.value);
    }

    private selectionChanged() {
        if (this.descriptor.max === 1) {
            this.onChange();
            return;
        }
        if (this._selectFormControl.value.length <= this.descriptor.max) {
            this._selections = this._selectFormControl.value;
            this.onChange();
        } else {
            const currentValue: string[] = _.cloneDeep(this._selectFormControl.value);
            this._selectFormControl.setValue(this._selections);
            const selectedIndex = this.descriptor.choices.map(choice => choice.name).findIndex(val =>
                !this._selections.includes(val) && currentValue.includes(val));
            if (selectedIndex > -1 && this._showMaxError.every(error => error === false)) {
                this._showMaxError[selectedIndex] = true;
                setTimeout(() => this._showMaxError[selectedIndex] = false, 5000);
            }
        }
    }


}