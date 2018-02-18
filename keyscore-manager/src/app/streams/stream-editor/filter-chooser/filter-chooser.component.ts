import {Component} from '@angular/core'
import {ModalService} from "../../../services/modal.service";
import {FilterDescriptor, FilterService, LOAD_FILTER_DESCRIPTORS} from "../../../services/filter.service";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs/Observable";
import {AppState} from "../../../app.component";
import {AddFilterAction} from "../../streams.actions";

@Component({
    selector: 'filter-chooser',
    template: require('./filter-chooser.component.html'),
    styles:[
        '.modal-lg{max-width:80% !important;}',
        '.list-group-item-action{cursor: pointer;}',
        '.active-group{background-color: cornflowerblue;color:rgb(255,255,255);transition: all 0.14s ease-in-out}'
        /*'.arrow_box:after, .arrow_box:before {left: 100%;top: 50%;border: solid transparent;content:" ";height: 0;width: 0; position: absolute;pointer-events: none;}',
        '.arrow_box:after {border-color: rgba(255, 255, 255, 0);border-left-color: #ffffff;border-width: 12px;margin-top: -12px;}',
        '.arrow_box:before {border-color: rgba(201, 201, 201, 0);border-left-color: #c9c9c9;border-width: 13px;margin-top: -13px;}',
        'button:active,button:focus{  border: 1px solid rgba(0, 255, 0, 0.125)!important;box-shadow: none !important;}'*/
    ],
    providers: [
        ModalService,
        FilterService
    ]
})
export class FilterChooser {

    private filterDescriptors$: Observable<FilterDescriptor[]>;
    private selectedFilterDescriptor: FilterDescriptor;
    private selectedGroup:string = 'sink';

    constructor(private store: Store<AppState>, private modalService: ModalService) {
        this.filterDescriptors$ = this.store.select(state => state.filterDescriptors);
        this.store.dispatch({type: LOAD_FILTER_DESCRIPTORS});
    }

    select(filterDescriptor: FilterDescriptor) {
        this.selectedFilterDescriptor = filterDescriptor;
    }

    add(filterDescriptor: FilterDescriptor) {
        this.store.dispatch(new AddFilterAction(this.selectedFilterDescriptor));
    }

    selectGroup(group:string){
        this.selectedGroup = group;
    }

    close() {
        this.modalService.close()
    }
}