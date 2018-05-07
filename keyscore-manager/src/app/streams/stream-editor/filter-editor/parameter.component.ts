import {Component, Input} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {Parameter, ParameterDescriptor} from "../../streams.model";


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
                    <label for="checkbox{{parameter.name}}" class="checkbox-label" data-off="no" data-on="yes"></label>

                </div>

                <div class="text-danger" *ngIf="!isValid">{{parameter.displayName}} {{'PARAMETERCOMPONENT.ISREQUIRED' | translate}}</div>
            </div>
        </div>

    `,
    styles:[
        '*,*:before,*:after{box-sizing: border-box;margin:0;padding:0;transition:0.25s ease-in-out;outline:none;}',
        '.ios-toggle,.ios-toggle:active{position:absolute; top:-5000px;height:0;width:0;opacity:0;border:none;outline:none;}',
        '.checkbox-label{display:block;position:relative;padding:10px;margin-bottom:20px;font-size:1em;line-height:16px;width:60px;height:36px;border-radius:18px;background:#f8f8f8;cursor:pointer;}',
        '.checkbox-label:before{content:"";display:block;position:absolute;z-index:1;line-height:34px;text-indent: 40px;height:36px;width:36px;border-radius: 100%;top:0;left:0;right:auto;background:white;box-shadow:0 3px 3px rgba(0,0,0,.2),0 0 0 2px #ddd;}',
        '.checkbox-label:after{content:attr(data-off);display:block;position:absolute;z-index:0;top:0;left:60px;padding:10px;height:100%;width:36px;color:#bfbfbf;white-space:nowrap;}',
        '.ios-toggle:checked + .checkbox-label{box-shadow:inset 0 0 0 20px rgba(19,191,17,1),0 0 0 2px rgba(19,191,17,1);}',
        '.ios-toggle:checked + .checkbox-label:before{box-shadow:0 0 0 2px transparent,0 3px 3px rgba(0,0,0,.3);left:calc(100% - 36px)}',
        '.ios-toggle:checked + .checkbox-label:after{content:attr(data-on);left:60px;width:36px;}',
        '.ios-toggle + .checkbox-label{box-shadow:inset 0 0 0 0px rgba(19,191,17,1),0 0 0 2px #dddddd;}',
        '.ios-toggle:checked + .checkbox-label{box-shadow:inset 0 0 0 18px rgba(19,191,17,1),0 0 0 2px rgba(19,191,17,1);}',
        '.ios-toggle:checked + .checkbox-label:after{color:rgb(21,187,48);}']
})
export class ParameterComponent {
    @Input() parameter: ParameterDescriptor;
    @Input() form: FormGroup;

    get isValid() {
        return this.form.controls[this.parameter.name].valid;
    }
}