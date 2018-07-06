import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {Go} from "../../router/router.actions";
import {ParameterControlService} from "../../services/parameter-control.service";
import {FilterConfiguration, Parameter, ParameterDescriptor} from "../pipelines.model";

@Component({
    selector: "pipeline-filter",
    template: `
        <div class="card mb-1">
            <div class="card-title">
                <div class="row pl-2 pt-2 pr-2">
                    <div class="col-auto btn-group-vertical">
                        <button type="button" class="btn btn-light" *ngIf="!(isEditingPipelineLocked$|async)"
                                (click)="moveFilter(filter.id, index - 1)" [disabled]="index == 0">
                            <img width="12em" src="/assets/images/chevron-up.svg"/>
                        </button>
                        <button type="button" class="btn btn-light" *ngIf="!(isEditingPipelineLocked$|async)"
                                (click)="moveFilter(filter.id, index + 1)" [disabled]="index == filterCount - 1">
                            <img width="12em" src="/assets/images/chevron-down.svg"/>
                        </button>
                    </div>
                    <div class="col" style="margin-top: auto; margin-bottom: auto">
                        <span class="font-weight-bold">{{filter.descriptor.displayName}}</span><br>
                        <small>{{filter.descriptor.description}}</small>
                    </div>

                    <div class="col-2"></div>
                    <div class="col-auto">
                        <button type="button" class="btn btn-primary"
                                *ngIf="!editing && !(isEditingPipelineLocked$|async)"
                                (click)="editFilter(filter.id)">{{'GENERAL.EDIT' | translate}}
                        </button>
                        <button type="button" class="btn btn-info" *ngIf="editing" (click)="callLiveEditing(filter)">
                            <img src="/assets/images/ic_settings_white_24px.svg" alt="Live Editing"/>
                        </button>
                        <button type="button" [disabled]="form.invalid" class="btn btn-success" *ngIf="editing"
                                (click)="saveFilter(filter,form.value)"><img src="/assets/images/ic_save_white.svg"
                                                                             alt="Save"/>
                        </button>

                        <button type="button" class="btn btn-secondary" *ngIf="editing"
                                (click)="cancelEditing()"><img src="/assets/images/ic_cancel_white_24px.svg"
                                                               alt="Cancel"/>
                        </button>
                        <button type="button" class="btn btn-danger" *ngIf="editing"
                                (click)="removeFilter(filter)"><img src="/assets/images/ic_delete_white_24px.svg"
                                                                    alt="Remove"/>
                        </button>
                    </div>
                </div>
                <form *ngIf="editing" class="form-horizontal col-12 mt-3" [formGroup]="form">

                    <div *ngFor="let parameter of parameters" class="form-row">
                        <app-parameter class="col-12" [parameterDescriptor]="parameter" [form]="form"></app-parameter>
                    </div>

                    <div class="form-row" *ngIf="payLoad">
                        {{'PIPELINECOMPONENT.SAVED_VALUES' | translate}}<br>{{payLoad}}
                    </div>

                </form>
            </div>

        </div>
    `,
    providers: [
        ParameterControlService
    ]
})
export class PipelineFilterComponent implements OnInit {

    @Input() public isEditingPipelineLocked$: Observable<boolean>;
    @Input() public filter: FilterConfiguration;
    @Input() public index: number;
    @Input() public parameters: ParameterDescriptor[];
    @Input() public filterCount: number;

    public editing: boolean = false;
    public payLoad: string = "";
    public form: FormGroup;

    @Output() private update: EventEmitter<{ filterConfiguration: FilterConfiguration, values: any }> =
        new EventEmitter();
    @Output() private move: EventEmitter<{ id: string, position: number }> = new EventEmitter();
    @Output() private remove: EventEmitter<FilterConfiguration> = new EventEmitter();
    @Output() private liveEdit: EventEmitter<FilterConfiguration> = new EventEmitter();

    constructor(private parameterService: ParameterControlService) {

    }

    public ngOnInit() {
        this.form = this.parameterService.toFormGroup(this.parameters, this.filter.parameters);
    }

    public removeFilter(filter: FilterConfiguration) {
        this.remove.emit(filter);
    }

    public moveFilter(id: string, position: number) {
        this.move.emit({id, position});
    }

    public editFilter(id: string) {
        this.editing = true;
    }

    public saveFilter(filterConfiguration: FilterConfiguration, values: any) {
        console.log(JSON.stringify(values));
        this.update.emit({filterConfiguration, values});
    }

    public cancelEditing() {
        this.editing = false;
        const resetFormValues = {};
        this.filter.descriptor.parameters.forEach((p) => resetFormValues[p.name] = p.value ? p.value : "");
        this.form.setValue(resetFormValues);
    }

    public callLiveEditing(filter: FilterConfiguration) {
        this.liveEdit.emit(filter);
    }

}
