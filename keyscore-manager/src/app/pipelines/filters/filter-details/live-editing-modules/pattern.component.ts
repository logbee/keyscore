import {Component, EventEmitter, Input, Output} from "@angular/core";

@Component({
    selector: 'pattern',
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold">
                {{'FILTERLIVEEDITINGCOMPONENT.REGEXPATTERN' | translate}}
            </div>
            <div class="card-body">
                <div class="form-group">
                    <textarea placeholder="{{'FILTERLIVEEDITINGCOMPONENT.REGEX_PLACEHOLDER' | translate}}"
                              class="form-control" rows="1"></textarea>
                </div>
                <button class="float-right btn primary btn-info" (click)="applyConfiguration('TestStringForRegex')"> {{'GENERAL.APPLY' | translate}}</button>
            </div>
        </div>
    `
})

export class PatternComponent {

    @Output() apply: EventEmitter<string> = new EventEmitter();

    constructor() {
    }

    applyConfiguration(regex: string) {
        this.apply.emit(regex)
    }
}



