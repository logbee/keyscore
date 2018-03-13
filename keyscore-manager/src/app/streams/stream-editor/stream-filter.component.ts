import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {FilterModel, Parameter} from "../streams.model";
import {ParameterControlService} from "../../services/parameter-control.service";
import {FormControl, FormGroup} from "@angular/forms";

@Component({
    selector: 'stream-filter',
    template: `
        <div class="card mb-1">
            <div class="card-title">
                <div class="row pl-2 pt-2 pr-2">
                    <div class="col-auto btn-group-vertical">
                        <button type="button" class="btn btn-light"
                                (click)="moveFilter(filter.id, index - 1)" [disabled]="index == 0">
                            <img width="12em" src="/assets/images/chevron-up.svg"/>
                        </button>
                        <button type="button" class="btn btn-light"
                                (click)="moveFilter(filter.id, index + 1)" [disabled]="index == filterCount - 1">
                            <img width="12em" src="/assets/images/chevron-down.svg"/>
                        </button>
                    </div>
                    <div class="col" style="margin-top: auto; margin-bottom: auto">
                        <span class="font-weight-bold">{{filter.displayName}}</span><br>
                        <small>{{filter.description}}</small>
                    </div>

                    <div class="col-2"></div>
                    <div class="col-auto">
                        <button type="button" class="btn btn-primary" *ngIf="!editing"
                                (click)="editFilter(filter.id)">Edit
                        </button>
                        <button type="button" [disabled]="!isEditingValid" class="btn btn-success" *ngIf="editing"
                                (click)="saveFilter()"><img src="/assets/images/ic_save_white.svg" alt="Save"/>
                        </button>

                        <button type="button" class="btn btn-secondary" *ngIf="editing"
                                (click)="cancelEditing()"><img src="/assets/images/ic_cancel_white_24px.svg" alt="Cancel"/>
                        </button>
                        <button type="button" class="btn btn-danger" *ngIf="editing"
                                (click)="removeFilter(filter)"><img src="/assets/images/ic_delete_white_24px.svg" alt="Remove"/>
                        </button>
                    </div>
                </div>
                <form *ngIf="editing" class="form-horizontal col-12 mt-3" [formGroup]="form">

                    <div *ngFor="let parameter of parameters" class="form-row">
                        <app-parameter class="col-12" [parameter]="parameter" [form]="form"></app-parameter>
                    </div>

                    <div class="form-row" *ngIf="payLoad">
                        Saved the following values<br>{{payLoad}}
                    </div>

                </form>
            </div>

        </div>
    `,
    providers: [
        ParameterControlService
    ]
})
export class StreamFilterComponent implements OnInit {

    @Input() filter: FilterModel;
    @Input() index: number;
    @Input() filterCount: number;
    @Input() parameters: Parameter[];

    @Output() update: EventEmitter<FilterModel> = new EventEmitter();
    @Output() move: EventEmitter<{ id: string, position: number }> = new EventEmitter();
    @Output() edit: EventEmitter<string> = new EventEmitter();
    @Output() remove: EventEmitter<FilterModel> = new EventEmitter();

    editing: boolean = false;
    isEditingValid: boolean = false;
    payLoad: string = '';
    form: FormGroup;


    constructor(private parameterService: ParameterControlService) {

    }

    ngOnInit() {
        this.form = this.parameterService.toFormGroup(this.parameters);
        this.form.valueChanges.subscribe(_ => this.form.valid ? this.isEditingValid = true : this.isEditingValid = false);

    }

    removeFilter(filter:FilterModel) {
        this.remove.emit(filter);
    }

    moveFilter(id: string, position: number) {
        this.move.emit({id, position});
    }

    editFilter(id: string) {
        this.editing = true;
        this.edit.emit(id);
    }

    saveFilter() {
        this.payLoad=JSON.stringify(this.form.value);
    }

    cancelEditing() {
        this.editing = false;
    }

    enableFilter() {
    }

    disableFilter() {
    }
}