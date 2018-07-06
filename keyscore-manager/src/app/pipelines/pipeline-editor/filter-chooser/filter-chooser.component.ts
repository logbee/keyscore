import {Component} from "@angular/core";
import {ModalService} from "../../../services/modal.service";

import {Store} from "@ngrx/store";
import {combineLatest, Observable, Subject} from "rxjs";
import {map} from "rxjs/internal/operators";
import {AddFilterAction, LoadFilterDescriptorsAction} from "../../pipelines.actions";
import {FilterDescriptor, getFilterCategories, getFilterDescriptors, PipelinesModuleState} from "../../pipelines.model";

@Component({
    selector: "filter-chooser",
    template: require("./filter-chooser.component.html"),
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

    constructor(private store: Store<PipelinesModuleState>, private modalService: ModalService) {
        this.filterDescriptors$ = this.store.select(getFilterDescriptors);
        this.categories$ = this.store.select(getFilterCategories);
        this.selectedCategory$ = new Subject();
        this.activeDescriptors$ = combineLatest(this.filterDescriptors$, this.selectedCategory$).pipe(
            map(([descriptors, category]) =>
                descriptors.filter((descriptor) => descriptor.category === category)));
        this.activeDescriptors$.subscribe((activeDescriptors) =>
            this.selectedFilterDescriptor = activeDescriptors[0]);
        this.store.dispatch(new LoadFilterDescriptorsAction());
    }

    public select(filterDescriptor: FilterDescriptor) {
        this.selectedFilterDescriptor = filterDescriptor;
    }

    public add(filterDescriptor: FilterDescriptor) {
        this.store.dispatch(new AddFilterAction(this.selectedFilterDescriptor));
    }

    public selectCategory(category: string) {
        this.selectedCategory$.next(category);
    }

    public close() {
        this.modalService.close();
    }

}
