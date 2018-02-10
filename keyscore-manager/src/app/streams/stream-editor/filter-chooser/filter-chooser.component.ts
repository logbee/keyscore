import {Component} from '@angular/core'
import {ModalService} from "../../../services/modal.service";
import {FilterDescriptor, FilterService, LOAD_FILTER_DESCRIPTORS} from "../../../services/filter.service";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs/Observable";
import {AddFilterAction} from "../stream-editor.actions";
import {AppState} from "../../../app.component";

@Component({
    selector: 'filter-chooser',
    template: require('./filter-chooser.component.html'),
    providers: [
        ModalService,
        FilterService
    ]
})
export class FilterChooser {

    private filterDescriptors$: Observable<FilterDescriptor[]>;
    private selectedFilterDescriptor: FilterDescriptor;

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

    close() {
        this.modalService.close()
    }
}