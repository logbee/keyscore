import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {FilterConfiguration} from "../../../models/filter-model/FilterConfiguration";
import {Store} from "@ngrx/store";
import {ParameterControlService} from "../../../services/parameter-control.service";
import {ParameterDescriptor} from "../../../models/pipeline-model/parameters/ParameterDescriptor";

@Component({
    selector: "filter-configuration",
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold" style="color: black;">
                {{'FILTERLIVEEDITINGCOMPONENT.REGEXPATTERN' | translate}}
            </div>
            <div class="card-body">
                <form *ngIf="true" class="form-horizontal col-12 mt-3" [formGroup]="form">
                    <div *ngFor="let parameter of parameters" class="form-row">
                        <app-parameter class="col-12" [parameterDescriptor]="parameter" [form]="form"></app-parameter>
                    </div>

                    <div class="form-row" *ngIf="payLoad">
                        {{'PIPELINECOMPONENT.SAVED_VALUES' | translate}}<br>{{payLoad}}
                    </div>
                </form>
                <button class="float-right btn primary btn-info mt-3"
                        (click)="applyFilter(filter,form.value)"> {{'GENERAL.APPLY' | translate}}
                </button>
            </div>
        </div>
    `
})

export class FilterConfigurationComponent implements OnInit {
    @Input() public parameters: ParameterDescriptor[];
    @Input() public filter: FilterConfiguration;
    public form: FormGroup;

    @Output() private apply: EventEmitter<{ filterConfiguration: FilterConfiguration, values: any }> =
        new EventEmitter();

    constructor(private parameterService: ParameterControlService, private store: Store<any>) {
    }

    public ngOnInit(): void {
        this.form = this.parameterService.toFormGroup(this.parameters, this.filter.parameters);
    }

    public applyFilter(filterConfiguration: FilterConfiguration, values: any) {
        console.log(JSON.stringify(values));
        this.apply.emit({filterConfiguration, values});
    }
}
