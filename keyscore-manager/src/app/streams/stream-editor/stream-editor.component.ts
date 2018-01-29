import {Component, OnInit} from '@angular/core';
import {Observable} from "rxjs/Observable";
import {Stream} from "./stream-editor.reducer";
import {FilterBlueprint, FilterService} from "../../services/filter.service"
import {Store} from "@ngrx/store";
import {ModalService} from "../../services/modal.service";
import {FilterChooser} from "./filter-chooser/filter-chooser.component";
import {
    DisableFilterAction,
    EditFilterAction,
    EnableFilterAction,
    MoveFilterAction,
    RemoveFilterAction,
    SaveFilterAction
} from "./stream-editor.actions";

@Component({
    selector: 'stream-editor',
    template: require('./stream-editor.component.html'),
    providers: [
        FilterService
    ]
})

export class StreamEditorComponent implements OnInit {
    stream$: Observable<Stream>;
    streamName: String;
    streamDescription: String;
    filterCount: number;
    filterComponents: FilterBlueprint[];

    constructor(private store: Store<any>, private filterService: FilterService, private modalService: ModalService) {
    }

    ngOnInit(): void {
        this.stream$ = this.store.select<any>('stream');
        this.stream$.subscribe(stream => {
            if (stream && stream.filters) {
                this.filterCount = stream.filters.length
            }
            else {
                this.filterCount = 0;
            }
        });
    }

    removeFilter(id: number) {
        this.store.dispatch(new RemoveFilterAction(id))

    }

    moveFilter(id: number, position: number) {
        this.store.dispatch(new MoveFilterAction(id, position));
    }

    editFilter(id: number) {
        this.store.dispatch(new EditFilterAction(id))
    }

    saveFilter(id: number) {
        this.store.dispatch(new SaveFilterAction(id))
    }

    enableFilter(id: number) {
        this.store.dispatch(new EnableFilterAction(id))
    }

    disableFilter(id: number) {
        this.store.dispatch(new DisableFilterAction(id))
    }

    addFilter(filter:FilterBlueprint){
        this.modalService.show(FilterChooser);
    }

    saveStream() {
        this.modalService.close();
    }
}
