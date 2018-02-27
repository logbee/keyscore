import {Component} from '@angular/core'
import {ModalService} from "../../../services/modal.service";

import {Store} from "@ngrx/store";
import {Observable} from "rxjs/Rx";
import {AddFilterAction, LoadFilterDescriptorsAction} from "../../streams.actions";
import {FilterDescriptor, getFilterCategories, getFilterDescriptors, StreamsState} from "../../streams.model";
import {Subject} from "rxjs/Subject";


@Component({
    selector: 'filter-chooser',
    template: require('./filter-chooser.component.html'),
    styles: [
        '.modal-lg{max-width:80% !important;}',
        '.list-group-item-action{cursor: pointer;}',
        '.active-group{background-color: cornflowerblue;color:rgb(255,255,255);transition: all 0.14s ease-in-out}'
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
    private categories$: Observable<string[]>;

    constructor(private store: Store<StreamsState>, private modalService: ModalService) {
        this.filterDescriptors$ = this.store.select(getFilterDescriptors);
        this.categories$ = this.store.select(getFilterCategories);
        this.selectedCategory$ = new Subject();
        this.activeDescriptors$ = this.filterDescriptors$.combineLatest(this.selectedCategory$).map(([descriptors, category]) => descriptors.filter(descriptor => descriptor.category == category));
        this.activeDescriptors$.subscribe(activeDescriptors => this.selectedFilterDescriptor=activeDescriptors[0]);
        this.store.dispatch(new LoadFilterDescriptorsAction());
    }

    select(filterDescriptor: FilterDescriptor) {
        this.selectedFilterDescriptor = filterDescriptor;
    }

    add(filterDescriptor: FilterDescriptor) {
        this.store.dispatch(new AddFilterAction(this.selectedFilterDescriptor));
    }

    selectCategory(category: string) {
        this.selectedCategory$.next(category);
    }

    close() {
        this.modalService.close()
    }

}

