import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {FilterConfiguration} from "../../../models/filter-model/FilterConfiguration";
import {Store} from "@ngrx/store";
import {ParameterControlService} from "../../../common/parameter/services/parameter-control.service";
import {ParameterDescriptor} from "../../../models/pipeline-model/parameters/ParameterDescriptor";
import {Observable} from "rxjs/index";
import {Dataset} from "../../../models/filter-model/dataset/Dataset";

import "../../styles/filterstyle.css";

@Component({
    selector: "filter-configuration",
    template: `
        <div class="card mt-3 card-body-background">
            <div id="custom-card-black" class="card-header alert-light font-weight-bold">
                {{'FILTERLIVEEDITINGCOMPONENT.REGEXPATTERN' | translate}}
            </div>
            <div *ngIf="!noDataAvailable; else noparam">
                <form *ngIf="!noParamsAvailable; else noparam" class="form-horizontal col-12 ml-1"
                      [formGroup]="form">
                    <div *ngFor="let parameter of parameters" class="form-row">
                        <app-parameter class="col-12" [parameterDescriptor]="parameter"
                                       [form]="form"></app-parameter>
                    </div>

                    <div class="form-row" *ngIf="payLoad">
                        {{'PIPELINECOMPONENT.SAVED_VALUES' | translate}}<br>{{payLoad}}
                    </div>
                </form>
                <div class="row mb-3">
                    <div class="col-sm-2">
                        <button *ngIf="!noParamsAvailable" title=" {{'GENERAL.APPLY_TITLE' | translate}}" class="float-left btn btn-success"
                                (click)="applyFilter(filter,form.value)">{{'GENERAL.APPLY' | translate}}
                            <img  width="20em" src="/assets/images/ic-remove-white.svg" alt=" {{'GENERAL.APPLY' | translate}}"/>
                        </button>
                    </div>
                    <div class="col-sm-10"></div>
                </div>
            </div>
        </div>
        
        <ng-template #noparam>
            <div class="col-sm-12 mt-3" align="center">
                <h4>{{'FILTERLIVEEDITINGCOMPONENT.NODATACONFIG' | translate}}</h4>
            </div>
        </ng-template>
    `
})

export class FilterConfigurationComponent implements OnInit {
    @Input() public filter$: Observable<FilterConfiguration>;
    @Input() public extractedDatasets$: Observable<Dataset[]>;

    public form: FormGroup;
    public filter: FilterConfiguration;
    public parameters: ParameterDescriptor[];
    private noParamsAvailable: boolean = true;
    private noDataAvailable: boolean = true;

    @Output() private apply: EventEmitter<{ filterConfiguration: FilterConfiguration, values: any }> =
        new EventEmitter();

    constructor(private parameterService: ParameterControlService, private store: Store<any>) {
    }

    public ngOnInit(): void {
        this.extractedDatasets$.subscribe((datasets) => {
            this.noDataAvailable = datasets.length === 0;
        });
        this.filter$.subscribe((filter) => {
            this.parameters = filter.descriptor.parameters;
            this.noParamsAvailable = filter.descriptor.parameters.length === 0;
            this.filter = filter;
        });
        this.form = this.parameterService.toFormGroup(this.parameters, this.filter.parameters);

    }

    public applyFilter(filterConfiguration: FilterConfiguration, values: any) {
        this.apply.emit({filterConfiguration, values});
    }
}
