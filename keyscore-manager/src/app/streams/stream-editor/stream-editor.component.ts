import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {Observable} from "rxjs/Observable";
import {FilterBlueprint, FilterService} from "../../services/filter.service"
import {Store} from "@ngrx/store";
import {ModalService} from "../../services/modal.service";
import {FilterChooser} from "./filter-chooser/filter-chooser.component";
import {
    DisableFilterAction, EditFilterAction, EnableFilterAction, MoveFilterAction, RemoveFilterAction,
    SaveFilterAction
} from "./stream-editor.actions";
import {getEditingStream, StreamModel} from "../streams.model";
import {DeleteStreamAction, ResetStreamAction, UpdateStreamAction} from "../streams.actions";

@Component({
    selector: 'stream-editor',
    template: require('./stream-editor.component.html'),
    providers: [
        FilterService
    ]
})
export class StreamEditorComponent implements OnInit {
    stream$: Observable<StreamModel>;
    streamId: string;
    streamName: string;
    streamDescription: string;
    editing: boolean = false;

    constructor(private store: Store<any>, private location: Location, private filterService: FilterService, private modalService: ModalService) {
    }

    ngOnInit(): void {
        this.stream$ = this.store.select(getEditingStream);
        this.stream$.subscribe(stream => {
            if (stream != null) {
                this.streamId = stream.id;
                this.streamName = stream.name;
                this.streamDescription = stream.description;
                this.editing = false;
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

    addFilter(filter: FilterBlueprint) {
        this.modalService.show(FilterChooser);
    }

    deleteStream() {
        this.store.dispatch(new DeleteStreamAction(this.streamId));
        this.location.back();
    }

    startStreamEditing() {
        this.editing = true;
    }

    saveStreamEditing() {
        this.store.dispatch(new UpdateStreamAction({
            id: this.streamId,
            name: this.streamName,
            description: this.streamDescription,
            filters: []
        }));
    }

    cancelStreamEditing() {
        this.store.dispatch(new ResetStreamAction(this.streamId))
    }
}
