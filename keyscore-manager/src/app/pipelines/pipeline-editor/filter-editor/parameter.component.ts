import {Component, Input} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {Parameter, ParameterDescriptor} from "../../pipelines.model";


@Component({
    selector: 'app-parameter',
    template: `
        <div [formGroup]="form">
            <label [attr.for]="parameter.name">{{parameter.displayName}}</label>
            <div [ngSwitch]="parameter.kind">
                <input class="form-control" *ngSwitchCase="'text'" [formControlName]="parameter.name"
                       [id]="parameter.name" [type]="'text'">

                <input class="form-control" *ngSwitchCase="'int'" [formControlName]="parameter.name"
                       [id]="parameter.name" [type]="'number'">
                <parameter-list *ngSwitchCase="'list'" [formControlName]="parameter.name"
                                [id]="parameter.name"></parameter-list>
                <parameter-map *ngSwitchCase="'map'" [formControlName]="parameter.name" [id]="parameter.name"></parameter-map>
                
                <div *ngSwitchCase="'boolean'" class="toggleCheckbox" [id]="parameter.name">

                    <input type="checkbox" id="checkbox{{parameter.name}}" class="ios-toggle" [formControlName]="parameter.name"> 
                    <label for="checkbox{{parameter.name}}" class="checkbox-label" data-off="" data-on=""></label>

                </div>

                <div class="text-danger" *ngIf="!isValid">{{parameter.displayName}} {{'PARAMETERCOMPONENT.ISREQUIRED' | translate}}</div>
            </div>
        </div>

    `
})
export class ParameterComponent {
    @Input() parameter: ParameterDescriptor;
    @Input() form: FormGroup;

    get isValid() {
        return this.form.controls[this.parameter.name].valid;
    }
}