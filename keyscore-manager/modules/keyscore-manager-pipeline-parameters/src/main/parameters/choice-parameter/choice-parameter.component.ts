import {Component} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {FormControl} from "@angular/forms";
import {animate, style, transition, trigger} from "@angular/animations";
import {cloneDeep} from 'lodash-es';
import {
    ChoiceParameter,
    ChoiceParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/choice-parameter.model";

@Component({
    selector: 'parameter-choice',
    template: `
        <mat-form-field>
            <mat-select [formControl]="selectFormControl" [multiple]="descriptor.max > 1 ? '' :null"
                        (selectionChange)="selectionChanged()">
                <mat-option *ngFor="let choice of descriptor.choices;let i=index" [value]="choice.name"
                            [matTooltip]="choice.description ||null"
                            matTooltipPosition="before">
                    <span>{{choice.displayName}}</span>

                    <div @max-warn *ngIf="showMaxError[i]" class="choice-max-error" translate
                         [translateParams]="{name:descriptor.displayName,max:descriptor.max}">
                        PARAMETER.CHOICE_MAX_SELECTION_WARNING
                    </div>
                </mat-option>
            </mat-select>
            <mat-label>{{descriptor.displayName}}</mat-label>
        </mat-form-field>
        <p class="parameter-warn" *ngIf=" descriptor.min > 1 && selectFormControl.value.length < descriptor.min"
           translate [translateParams]="{name:descriptor.displayName,min:descriptor.min}">
            PARAMETER.CHOICE_MIN_SELECTION_WARNING
        </p>
    `,
    styleUrls: ['../../style/parameter-module-style.scss'],
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
        return new ChoiceParameter(this.descriptor.ref, this.selectFormControl.value)
    }

    selectFormControl: FormControl;
    selections: string[];
    showMaxError: boolean[] = [];

    protected onInit() {
        this.selectFormControl = new FormControl(this.parameter.value || "");
        this.descriptor.choices.forEach(_ => this.showMaxError.push(false));
    }

    onChange() {
        this.emit(this.value);
    }

    selectionChanged() {
        if (this.descriptor.max === 1) {
            this.onChange();
            return;
        }
        if (this.selectFormControl.value.length <= this.descriptor.max) {
            this.selections = this.selectFormControl.value;
            this.onChange();
            return;
        }
        const currentValue: string[] = cloneDeep(this.selectFormControl.value);
        this.selectFormControl.setValue(this.selections);
        const selectedIndex = this.descriptor.choices.map(choice => choice.name).findIndex(val =>
            !this.selections.includes(val) && currentValue.includes(val));
        if (selectedIndex > -1 && this.showMaxError.every(error => error === false)) {
            this.showMaxError[selectedIndex] = true;
            setTimeout(() => this.showMaxError[selectedIndex] = false, 5000);
        }

    }


}
