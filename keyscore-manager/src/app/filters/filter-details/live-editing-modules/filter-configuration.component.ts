import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {FilterConfiguration} from "../../../models/filter-model/FilterConfiguration";
import {Store} from "@ngrx/store";
import {ParameterControlService} from "../../../services/parameter-control.service";
import {ParameterDescriptor} from "../../../models/pipeline-model/parameters/ParameterDescriptor";
import {Observable} from "rxjs/index";
import {Dataset} from "../../../models/filter-model/dataset/Dataset";

@Component({
    selector: "filter-configuration",
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold" style="color: black;">
                {{'FILTERLIVEEDITINGCOMPONENT.REGEXPATTERN' | translate}}
            </div>
            <div class="card-body">
                <div *ngIf="!noDataAvailable; else noparam">
                    <form *ngIf="!noParamsAvailable; else noparam" class="form-horizontal col-12 mt-3"
                          [formGroup]="form">
                        <div *ngFor="let parameter of parameters" class="form-row">
                            <app-parameter class="col-12" [parameterDescriptor]="parameter"
                                           [form]="form"></app-parameter>
                        </div>

                        <div class="form-row" *ngIf="payLoad">
                            {{'PIPELINECOMPONENT.SAVED_VALUES' | translate}}<br>{{payLoad}}
                        </div>
                    </form>
                </div>
                <button *ngIf="!noParamsAvailable" class="float-right btn primary btn-info mt-3"
                        (click)="applyFilter(filter,form.value)"> {{'GENERAL.APPLY' | translate}}
                </button>
            </div>
        </div>
        <ng-template #noparam>
            <div class="col-sm-12" align="center">
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
