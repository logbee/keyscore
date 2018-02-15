import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {Observable} from "rxjs/Observable";
import {FilterService} from "../../services/filter.service"
import {Store} from "@ngrx/store";
import {ModalService} from "../../services/modal.service";
import {FilterChooser} from "./filter-chooser/filter-chooser.component";
import {getEditingStream, StreamModel} from "../streams.model";
import {DeleteStreamAction, MoveFilterAction, ResetStreamAction, UpdateStreamAction} from "../streams.actions";

@Component({
    selector: 'stream-editor',
    template: `
        <div class="row justify-content-center">
            <div class="col-3">
                <stream-details [stream]="stream$ | async"
                                [locked]="isLocked"
                                (update)="updateStream($event)"
                                (reset)="resetStream($event)"
                                (delete)="deleteStream($event)"
                                (lock)="setLocked(true, $event)"
                                (unlock)="setLocked(false, $event)">
                </stream-details>
            </div>
            <div class="col-9">
                <div class="card">
                    <div class="card-header d-flex justify-content-between">
                        <span class="font-weight-bold">Structure</span>
                        <div *ngIf="!isLocked">
                            <button class="btn btn-success" (click)="addFilter(null)">Add Filter</button>
                        </div>
                    </div>
                    <div class="card-body">
                        <stream-filter *ngFor="let filter of (stream$ | async).filters; index as i" [filter]="filter" [index]="i"
                                       (move)="moveFilter($event)">
                        </stream-filter>
                    </div>
                </div>
            </div>
        </div>
    `,
    providers: [
        FilterService
    ]
})
export class StreamEditorComponent implements OnInit {
    stream$: Observable<StreamModel>;
    isLocked: boolean;

    constructor(private store: Store<any>, private location: Location, private filterService: FilterService, private modalService: ModalService) {
    }

    ngOnInit(): void {
        this.stream$ = this.store.select(getEditingStream);
        this.stream$.subscribe(stream => {
            this.isLocked = (stream.filters && stream.filters.length > 0)
        })
    }

    addFilter(stream: StreamModel) {
        this.modalService.show(FilterChooser);
    }

    deleteStream(stream: StreamModel) {
        this.store.dispatch(new DeleteStreamAction(stream.id));
        this.location.back();
    }

    updateStream(stream: StreamModel) {
        this.store.dispatch(new UpdateStreamAction({
            id: stream.id,
            name: stream.name,
            description: stream.description,
            filters: stream.filters
        }));
    }

    resetStream(stream: StreamModel) {
        this.store.dispatch(new ResetStreamAction(stream.id))
    }

    setLocked(locked: boolean, stream: StreamModel) {
        this.isLocked = locked;
    }

    moveFilter(filter:{id:string,position:number}) {
        this.store.dispatch(new MoveFilterAction(filter.id, filter.position))
    }
}
