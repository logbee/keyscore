import {Component} from '@angular/core'
import {ModalService} from "../../../services/modal.service";

import {Store} from "@ngrx/store";
import {Observable} from "rxjs/Rx";
import {AddFilterAction, LoadFilterDescriptorsAction} from "../../streams.actions";
import {FilterDescriptor, getFilterDescriptors, StreamsState} from "../../streams.model";
import {Subject} from "rxjs/Subject";


@Component({
    selector: 'filter-chooser',
    template: require('./filter-chooser.component.html'),
    styles: [
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
    ]
})


export class FilterChooser {

    private filterDescriptors$: Observable<FilterDescriptor[]>;


    private selectedFilterDescriptor: FilterDescriptor;
    private selectedCategory$: Subject<string>;
    private activeDescriptors$: Observable<FilterDescriptor[]>;
    private categories$:Observable<string[]>;

    constructor(private store: Store<StreamsState>, private modalService: ModalService) {
        this.filterDescriptors$ = this.store.select(getFilterDescriptors);
        this.filterDescriptors$.subscribe(descriptors => this.categories$ = Observable.from(unique(descriptors.map(descriptor => descriptor.category))))
        this.activeDescriptors$ = this.filterDescriptors$.combineLatest(this.selectedCategory$).map(([descriptors, category]) => descriptors.filter(descriptor => descriptor.category == category))
        this.store.dispatch(new LoadFilterDescriptorsAction());
    }

    select(filterDescriptor: FilterDescriptor) {
        this.selectedFilterDescriptor = filterDescriptor;
    }

    add(filterDescriptor: FilterDescriptor) {
        this.store.dispatch(new AddFilterAction(this.selectedFilterDescriptor));
    }

    selectCategory(category: string) {
        this.selectedCategory$.next(category)
    }

    close() {
        this.modalService.close()
    }


}

function unique(a:string[]) {
    var seen = {};
    return a.filter(function(item) {
        return seen.hasOwnProperty(item) ? false : (seen[item] = true);
    });
}