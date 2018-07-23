import {Component, EventEmitter, OnInit, Output} from "@angular/core";
import {FormGroup, FormBuilder, Validators, FormControl} from "@angular/forms";
import {s} from "@angular/core/src/render3";
import {FilterConfiguration} from "../../../models/filter-model/FilterConfiguration";
import {Store} from "@ngrx/store";
import {selectLiveEditingFilter} from "../../filter.reducer";

@Component({
    selector: "pattern",
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold" style="color: black;">
                {{'FILTERLIVEEDITINGCOMPONENT.REGEXPATTERN' | translate}}
            </div>
            <div class="card-body">
                <div class="row mb-3">
                    <div class="col-sm-12">
                        <strong>{{'FILTERLIVEEDITINGCOMPONENT.FIELDNAMES' | translate}}:</strong>
                    </div>
                </div>
                <div class="row mb-3">
                    <div class="col-sm-4">
                        <input class="form-control" [formControl]="fieldNameControl" type="text"
                               (keydown.enter)="addField()"
                               placeholder="{{'FILTERLIVEEDITINGCOMPONENT.FIELDNAMES' | translate}}"/>
                    </div>
                    <div class="col-sm-8">
                        <button class="btn btn-success" (click)="addField()">+</button>
                   </div>
                </div>
                <div class="row mb-3 ml-2">
                    <div class="col-sm-12">
                        <div *ngFor="let element of fieldNameList let i = index " style="float: left">
                               <span class="tag">
                                    <small>{{element }}
                                        <span class="removeX" (click)="removefromfieldNameList(i)">x</span>
                                    </small>
                               </span>
                        </div>
                    </div>
                </div>
                <div class="row mb-3">
                    <div class="col-12">
                        <input type="text" [formControl]="patternControl"
                               class="form-control input-group input-group-lg"
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
    public patternControl = new FormControl();
    public fieldNameControl = new FormControl();
    private currentRegex: string;
    private fieldNameList: string[] = [];
    private fieldName: string;
    private newConfiguration: FilterConfiguration;

    constructor(private store: Store<any>) {
    }
    public applyConfiguration() {
        this.store.select(selectLiveEditingFilter).subscribe((filter) => this.newConfiguration = filter);
        this.apply.emit(this.newConfiguration);
    }

    public ngOnInit(): void {
        this.patternControl.valueChanges.subscribe((value) => {
            this.currentRegex = value;
        });
        this.fieldNameControl.valueChanges.subscribe((value) => {
            this.fieldName = value;
        });
    }

    private addField() {
        if (this.fieldName !== "") {
            this.fieldNameList.push(this.fieldName);
            this.fieldNameControl.setValue("");
        }
    }

    private removefromfieldNameList(pos: number) {
        this.fieldNameList = this.fieldNameList.filter((element) => element !== this.fieldNameList[pos]);
    }
}
