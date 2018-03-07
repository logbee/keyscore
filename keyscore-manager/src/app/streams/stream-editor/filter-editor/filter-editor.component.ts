import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core'
import {ModalService} from "../../../services/modal.service";

import {Store} from "@ngrx/store";
import {Observable} from "rxjs/Rx";
import {AddFilterAction, LoadFilterDescriptorsAction} from "../../streams.actions";
import {
    FilterDescriptor, FilterModel, getEditingFilter, getEditingFilterParameters, getFilterCategories,
    getFilterDescriptors, Parameter,
    ParameterDescriptor,
    StreamsState
} from "../../streams.model";
import {Subject} from "rxjs/Subject";
import {FormControl, FormGroup} from "@angular/forms";
import {ParameterControlService} from "../../../services/parameter-control.service";


@Component({
    selector: 'filter-editor',
    template: `
        <form class="form-horizontal col-12 mt-3" [formGroup]="form">

            <div *ngFor="let parameter of filterParameters" class="form-row">
                <app-parameter class="col-12" [parameter]="parameter" [form]="form"></app-parameter>
            </div>

            <div class="form-row" *ngIf="payLoad">
                Saved the following values<br>{{payLoad}}
            </div>


        </form>
    `,
    providers: [
        ParameterControlService
    ]
})


export class FilterEditorComponent implements OnInit {

    @Input() filterParameters: Parameter[];

    @Output() submitable:EventEmitter<string> = new EventEmitter();


    form: FormGroup;



    constructor(private parameterService: ParameterControlService) {

    }

    ngOnInit() {
        this.form = this.parameterService.toFormGroup(this.filterParameters);
        this.form.valueChanges.subscribe(_ =>this.form.valid ? this.submitable.emit(this.form.value) : this.submitable.emit(null) )
    }

}

