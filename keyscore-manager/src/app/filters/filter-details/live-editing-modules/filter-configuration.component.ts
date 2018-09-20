import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {Configuration} from "../../../models/common/Configuration";
import {Store} from "@ngrx/store";
import {
    ParameterDescriptor,
    ResolvedParameterDescriptor
} from "../../../models/parameters/ParameterDescriptor";
import {Observable} from "rxjs/index";
import {Dataset} from "../../../models/dataset/Dataset";
import "../../styles/filterstyle.css";

@Component({
    selector: "filter-configuration",
    template: `
        <mat-card>
            <mat-card-header class="fix-div">
                <div class="container" fxFlexFill="" fxLayout="row" fxLayout.xs="column">
                    <div fxFlexAlign="start" fxFlex="100%">
                        <mat-card-title>
                            <h1 class="mat-headline font-weight-bold">
                                {{'FILTERLIVEEDITINGCOMPONENT.REGEXPATTERN' | translate}}
                            </h1>
                        </mat-card-title>
                    </div>
                </div>
            </mat-card-header>
            <mat-card-content>
                <div fxFill="" fxLayoutGap="5px" fxLayout="column" fxLayout.xs="row">
                    <div fxFill="" fxLayoutGap="15px" fxLayout="column" *ngIf="!noDataAvailable; else noparam">
                        <form *ngIf="!noParamsAvailable; else noparam"
                              [formGroup]="form">
                            <div *ngFor="let parameter of parameters">
                                <app-parameter [parameterDescriptor]="parameter"
                                               [form]="form"></app-parameter>
                            </div>
                            <div *ngIf="payLoad">
                                {{'PIPELINECOMPONENT.SAVED_VALUES' | translate}}<br>{{payLoad}}
                            </div>
                        </form>
                        <div>
                            <button mat-raised-button="" color="primary" *ngIf="!noParamsAvailable" title=" {{'GENERAL.APPLY_TITLE' | translate}}"
                                    (click)="applyFilter(filter,form.value)">{{'GENERAL.APPLY' | translate}}
                                    <mat-icon>check_circle_outline</mat-icon>
                            </button>
                        </div>
                    </div>
                </div>
            </mat-card-content>
        </mat-card>
        
        <!--<div class="card mt-3 card-body-background">-->
        <!--<div id="custom-card-black" class="card-header alert-light font-weight-bold">-->
        <!--{{'FILTERLIVEEDITINGCOMPONENT.REGEXPATTERN' | translate}}-->
        <!--</div>-->
        <!--<div *ngIf="!noDataAvailable; else noparam">-->
        <!--<form *ngIf="!noParamsAvailable; else noparam" class="form-horizontal col-12 ml-1"-->
        <!--[formGroup]="form">-->
        <!--<div *ngFor="let parameter of parameters" class="form-row">-->
        <!--<app-parameter class="col-12" [parameterDescriptor]="parameter"-->
        <!--[form]="form"></app-parameter>-->
        <!--</div>-->

        <!--<div class="form-row" *ngIf="payLoad">-->
        <!--{{'PIPELINECOMPONENT.SAVED_VALUES' | translate}}<br>{{payLoad}}-->
        <!--</div>-->
        <!--</form>-->
        <!--<div class="row mb-3">-->
        <!--<div class="col-sm-2">-->
        <!--<button *ngIf="!noParamsAvailable" title=" {{'GENERAL.APPLY_TITLE' | translate}}" class="float-left btn btn-success"-->
        <!--(click)="applyFilter(filter,form.value)">{{'GENERAL.APPLY' | translate}}-->
        <!--<img  width="20em" src="/assets/images/ic-remove-white.svg" alt=" {{'GENERAL.APPLY' | translate}}"/>-->
        <!--</button>-->
        <!--</div>-->
        <!--<div class="col-sm-10"></div>-->
        <!--</div>-->
        <!--</div>-->
        <!--</div>-->

        <ng-template #noparam>
            <div class="col-sm-12 mt-3" align="center">
                <h4>{{'FILTERLIVEEDITINGCOMPONENT.NODATACONFIG' | translate}}</h4>
            </div>
        </ng-template>
    `
})

export class FilterConfigurationComponent implements OnInit {
    @Input() public filter$: Observable<Configuration>;
    @Input() public extractedDatasets$: Observable<Dataset[]>;

    public form: FormGroup;
    public filter: Configuration;
    public parameters: ResolvedParameterDescriptor[];
    private noParamsAvailable: boolean = true;
    private noDataAvailable: boolean = true;

    @Output() private apply: EventEmitter<{ filterConfiguration: Configuration, values: any }> =
        new EventEmitter();

    constructor(private store: Store<any>) {
    }

    public ngOnInit(): void {
        this.extractedDatasets$.subscribe((datasets) => {
            this.noDataAvailable = datasets.length === 0;
        });

        //Uncommented while changing api to get build

        /*this.filter$.subscribe((filter) => {
            this.parameters = filter.descriptor.parameters;
            this.noParamsAvailable = filter.descriptor.parameters.length === 0;
            this.filter = filter;
        });*/
       // this.form = this.parameterService.toFormGroup(this.parameters, this.filter.parameters);

    }

    public applyFilter(filterConfiguration: Configuration, values: any) {
        this.apply.emit({filterConfiguration, values});
    }
}
