import {Component} from '@angular/core'
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
    template: require('./filter-editor.component.html'),
    styles: [
        '.modal-lg{max-width:80% !important;}',
        '.list-group-item-action{cursor: pointer;}',
        '.active-group{background-color: cornflowerblue;color:rgb(255,255,255);transition: all 0.14s ease-in-out}'
    ],
    providers: [
        ModalService,
        ParameterControlService
    ]
})


export class FilterEditor {

    form:FormGroup;
    payLoad = '';

    private filterParameters$: Observable<Parameter[]>;
    private filter$:Observable<FilterModel>;

    constructor(private store: Store<StreamsState>, private modalService: ModalService,private parameterService: ParameterControlService) {
        this.filterParameters$ = this.store.select(getEditingFilterParameters);
        this.filter$ = this.store.select(getEditingFilter);
        this.filterParameters$.subscribe(parameters => this.form = this.parameterService.toFormGroup(parameters));
    }

    close(){
        this.modalService.close()
    }

    onSubmit(){
        this.payLoad=JSON.stringify(this.form.value);
    }


}

