import {Component} from '@angular/core'
import {ModalService} from "../../../services/modal.service";

import {Store} from "@ngrx/store";
import {Observable} from "rxjs/Rx";
import {AddFilterAction, LoadFilterDescriptorsAction} from "../../streams.actions";
import {
    FilterDescriptor, getEditedFilterParameters, getFilterCategories, getFilterDescriptors,
    ParameterDescriptor,
    StreamsState
} from "../../streams.model";
import {Subject} from "rxjs/Subject";


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
    ]
})


export class FilterEditor {

    private filterParameters$: Observable<ParameterDescriptor[]>;


    constructor(private store: Store<StreamsState>, private modalService: ModalService) {
        this.filterParameters$ = this.store.select(getEditedFilterParameters);
        console.log(this.filterParameters$)

    }


}

