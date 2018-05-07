import {Component, OnInit} from '@angular/core';
import {Location} from '@angular/common';
import {Observable} from "rxjs/Observable";
import {Store} from "@ngrx/store";
import {ModalService} from "../../services/modal.service";
import {FilterChooser} from "./filter-chooser/filter-chooser.component";
import {FilterModel, getEditingStream, getEditingStreamIsLocked, StreamModel} from "../streams.model";
import {
    DeleteStreamAction,MoveFilterAction, ResetStreamAction,
    UpdateStreamAction, RemoveFilterAction, LockEditingStreamAction, UpdateFilterAction
} from "../streams.actions";
import {selectAppConfig} from "../../app.config";
import {AppState} from "../../app.component";

@Component({
    selector: 'stream-editor',
    template: `
        <div class="row justify-content-center">
            <div class="col-3">
                <stream-details [stream]="stream$ | async"
                                [locked$]="isLocked$"
                                (update)="updateStream($event)"
                                (reset)="resetStream($event)"
                                (delete)="deleteStream($event)"
                                (lock)="setLocked(true, $event)"
                                (unlock)="setLocked(false, $event)">
                </stream-details>
            </div>
            
            <div *ngIf="!blocklyFlag" class="col-9">
                <div class="card">
                    <div class="card-header d-flex justify-content-between">
                        <span class="font-weight-bold">{{'STREAMEDITORCOMPONENT.STREAMBLUEPRINT' | translate}}</span>
                        <div *ngIf="!(isLocked$ | async)">
                            <button class="btn btn-success" (click)="addFilter(null)">Add Filter</button>
                        </div>
                    </div>
                    <div class="card-body">
                        <stream-filter class="filter-component" *ngFor="let filter of (stream$ | async).filters; index as i"
                                       [filter]="filter"
                                       [index]="i"
                                       [filterCount]="(stream$|async).filters.length"
                                       [parameters]="filter.parameters"
                                       [isEditingStreamLocked$]="isLocked$"
                                       (move)="moveFilter($event)"
                                       (remove)="removeFilter($event)"
                                       (update)="updateFilter($event)">
                        </stream-filter>
                    </div>
                </div>
            </div>
            <blockly-workspace *ngIf="blocklyFlag" class="col-9"></blockly-workspace>
        </div>
    `,
    styles:['.filter-component{transition: 0.25s ease-in-out;}'],
    providers: []
})
export class StreamEditorComponent implements OnInit {
    stream$: Observable<StreamModel>;
    isLocked$: Observable<boolean>;
    blocklyFlag:boolean;

    constructor(private store: Store<any>, private location: Location, private modalService: ModalService) {
        let config = this.store.select(selectAppConfig);
        config.subscribe(conf => this.blocklyFlag = conf.getBoolean('keyscore.manager.blockly'));
    }

    ngOnInit(): void {
        this.isLocked$ = this.store.select(getEditingStreamIsLocked);
        this.stream$ = this.store.select(getEditingStream);
        this.stream$.subscribe(stream => {
            this.store.dispatch(new LockEditingStreamAction(stream.filters && stream.filters.length > 0))
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
        //this.isLocked = locked;
        this.store.dispatch(new LockEditingStreamAction(locked))
    }

    moveFilter(filter: { id: string, position: number }) {
        this.store.dispatch(new MoveFilterAction(filter.id, filter.position))
    }


    updateFilter(update: { filterModel: FilterModel, values: any }) {
        this.store.dispatch(new UpdateFilterAction(update.filterModel, update.values))
    }

    removeFilter(filter: FilterModel) {
        this.store.dispatch(new RemoveFilterAction(filter.id))
    }
}
