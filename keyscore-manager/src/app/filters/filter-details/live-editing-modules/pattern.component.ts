import {Component, EventEmitter, OnInit, Output} from "@angular/core";
import {FormGroup, FormBuilder, Validators, FormControl} from "@angular/forms";
import {s} from "@angular/core/src/render3";
import {FilterConfiguration} from "../../../models/filter-model/FilterConfiguration";

@Component({
    selector: "pattern",
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold" style="color: black;">
                {{'FILTERLIVEEDITINGCOMPONENT.REGEXPATTERN' | translate}}
            </div>
            <div class="card-body">
                <div class="row mb-3">
                    <div class="col-sm-2">
                        <strong>{{'FILTERLIVEEDITINGCOMPONENT.PAUSED' | translate}}</strong>
                    </div>
                    <div class="col-sm-8" align="left">
                        <input type="checkbox" class="checkbox" [formControl]="isPausedControl"/>
                    </div>
                </div>
                <div class="row mb-3">
                    <div class="col-sm-12">
                        <strong>{{'FILTERLIVEEDITINGCOMPONENT.FIELDNAMES' | translate}}:</strong>
                    </div>
                </div>
                <div class="row mb-3">
                    <div class="col-sm-4">
                        <input class="form-control" [formControl]="fieldNameControl" type="text" placeholder="{{'FILTERLIVEEDITINGCOMPONENT.FIELDNAMES' | translate}}"/>
                    </div>
                    <div class="col-sm-8">
                        <button class="btn btn-success" (click)="addField()">+</button>
                    </div>
                </div>
                <div class="row mb-3">
                    <div class="col-sm-12">
                        <!--DummyCode-->
                        <span class="mr-2" style="background-color: #2a5263">
                        <small style=" color: white;"> test x </small>
                        </span>
                        <span class="mr-2" style="background-color: #369db4">
                        <small style=" color: white;"> name x </small>
                        </span>
                        <span class="mr-2" style="background-color: #325d83">
                        <small style=" color: white;"> name x </small>
                        </span>
                    </div>
                </div>
                <div class="row mb-3">
                    <div class="col-12">
                        <input type="text" [formControl]="patternControl" class="form-control input-group input-group-lg"
                               placeholder="{{'FILTERLIVEEDITINGCOMPONENT.REGEX_PLACEHOLDER' | translate}}"/>
                    </div>
                </div>

                <button class="float-right btn primary btn-info mt-3"
                        (click)="applyConfiguration()"> {{'GENERAL.APPLY' | translate}}
                </button>
            </div>
        </div>
    `
})

export class PatternComponent implements OnInit {
    @Output() public apply: EventEmitter<FilterConfiguration> = new EventEmitter();
    private currentRegex: string;
    private isPaused: boolean;
    private fieldnameList: string[] = [];
    private fieldName: string;
    patternControl = new FormControl();
    isPausedControl = new FormControl();
    fieldNameControl = new FormControl();

    public applyConfiguration() {
        //TODO: create Filterconfiguration with the new set Parameters
        this.apply.emit();
    }

    ngOnInit(): void {
        this.patternControl.valueChanges.subscribe(value => {
            this.currentRegex = value;
        });

        this.isPausedControl.valueChanges.subscribe(value => {
            this.isPaused = value;
        });

        this.fieldNameControl.valueChanges.subscribe(value => {
            this.fieldName = value;
        });
    }

    private addField() {
        this.fieldnameList.push(this.fieldName);
        this.fieldNameControl.setValue("");
    }

}
