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
    template: `
        <div class="row justify-content-center">
            <div class="col-3">
                <stream-details [stream]="stream$ | async"
                                (update)="updateStream($event)"
                                (reset)="resetStream($event)"
                                (delete)="deleteStream($event)">
                </stream-details>
            </div>
            <div class="col-9">
                <div class="card mb-1" *ngFor="let filter of (stream$ | async).filters; let i = index">
                    <div class="card-title">
                        <div class="row pl-2 pt-2 pr-2">
                            <div class="col-auto btn-group-vertical">
                                <button type="button" class="btn btn-light font-weight-bold"
                                        (click)="moveFilter(filter.id, i - 1)" [disabled]="i == 0">U
                                </button>
                                <button type="button" class="btn btn-light font-weight-bold"
                                        (click)="moveFilter(filter.id, i + 1)" [disabled]="i == filterCount - 1">D
                                </button>
                            </div>
                            <div class="col" style="margin-top: auto; margin-bottom: auto" *ngIf="!filter.editing">
                                <span class="font-weight-bold">{{filter.name}}</span><br>
                                <small>{{filter.description}}</small>
                            </div>
                            <div class="col" style="margin-top: auto; margin-bottom: auto" *ngIf="filter.editing">
                                <input id="filterName" class="form-control" placeholder="Name"
                                       [(ngModel)]="filter.name"/>
                                <input id="filterDescription" class="form-control" placeholder="Description"
                                       [(ngModel)]="filter.description"/>
                            </div>
                            <div class="col-2"></div>
                            <div class="col-auto">
                                <button type="button" class="btn btn-primary" *ngIf="!filter.editing"
                                        (click)="editFilter(filter.id)">Edit
                                </button>
                                <button type="button" class="btn btn-danger" *ngIf="filter.editing"
                                        (click)="removeFilter(filter.id)">Remove
                                </button>
                                <button type="button" class="btn btn-primary" *ngIf="filter.editing"
                                        (click)="saveFilter(filter.id)">Save
                                </button>
                            </div>
                        </div>
                    </div>
                    <div class="card-body" *ngIf="filter.editing">
                        <h6 class="font-weight-bold">Filter 1</h6>
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

    constructor(private store: Store<any>, private location: Location, private filterService: FilterService, private modalService: ModalService) {
    }

    ngOnInit(): void {
        this.stream$ = this.store.select(getEditingStream);
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

    deleteStream(stream: StreamModel) {
        this.store.dispatch(new DeleteStreamAction(stream.id));
        this.location.back();
    }

    updateStream(stream: StreamModel) {
        this.store.dispatch(new UpdateStreamAction({
            id: stream.id,
            name: stream.name,
            description: stream.description,
            filters: []
        }));
    }

    resetStream(stream: StreamModel) {
        this.store.dispatch(new ResetStreamAction(stream.id))
    }
}
