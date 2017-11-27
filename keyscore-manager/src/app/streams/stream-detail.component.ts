import {Component, OnInit} from '@angular/core';
import {Observable} from "rxjs/Observable";
import {
    DisableFilterAction,
    EditFilterAction, EnableFilterAction, FilterInstance, MoveFilterAction, RemoveFilterAction, SaveFilterAction,
    Stream
} from "./stream.reducer";
import {Store} from "@ngrx/store";
import {AppState} from "../app.component";

@Component({
    selector: 'keyscore-stream-detail',
    template: `
        <div class="row justify-content-center">
            <div class="col-3">
                <div class="card">
                    <div class="card-header">
                        <h4 class="">Stream</h4>
                    </div>
                    <div class="card-body">
                        <label for="streamName" class="font-weight-bold">Name</label>
                        <input id="streamName" class="form-control" placeholder="Name" [(ngModel)]="streamName"/>
                        <label for="streamDescription" class="font-weight-bold">Description</label>
                        <textarea id="streamDescription" class="form-control" placeholder="Description" rows="3" [(ngModel)]="streamDescription"></textarea>
                    </div>
                </div>
            </div>
            <div class="col-9">
                <div class="card mb-1" *ngFor="let filter of (stream$ | async).filters; let i = index">
                    <div class="card-title">
                        <div class="row pl-2 pt-2 pr-2">
                            <div class="col-auto btn-group-vertical">
                                <button type="button" class="btn btn-light font-weight-bold" (click)="moveFilter(filter.id, i - 1)" [disabled]="i == 0">U</button>
                                <button type="button" class="btn btn-light font-weight-bold" (click)="moveFilter(filter.id, i + 1)" [disabled]="i == filterCount - 1">D</button>
                            </div>
                            <div class="col" style="margin-top: auto; margin-bottom: auto" *ngIf="!filter.editing">
                                <span class="font-weight-bold">{{filter.name}}</span><br>
                                <small>{{filter.description}}</small>
                            </div>
                            <div class="col" style="margin-top: auto; margin-bottom: auto" *ngIf="filter.editing">
                                <input id="filterName" class="form-control" placeholder="Name" [(ngModel)]="filter.name"/>
                                <input id="filterDescription" class="form-control" placeholder="Description" [(ngModel)]="filter.description"/>
                            </div>
                            <div class="col-2"></div>
                            <div class="col-auto">
                                <button type="button" class="btn btn-primary" *ngIf="!filter.editing" (click)="editFilter(filter.id)">Edit</button>
                                <button type="button" class="btn btn-danger" *ngIf="filter.editing" (click)="removeFilter(filter.id)">Remove</button>
                                <button type="button" class="btn btn-primary" *ngIf="filter.editing" (click)="saveFilter(filter.id)">Save</button>
                            </div>
                        </div>
                    </div>
                    <div class="card-body" *ngIf="filter.editing">
                        <h6 class="font-weight-bold">Filter 1</h6>
                    </div>
                </div>
            </div>
        </div>
    `
})

export class StreamDetailComponent implements OnInit {
    stream$: Observable<Stream>;
    streamName: String;
    streamDescription: String;
    filterCount: number;

    constructor(private store: Store<any>) {}

    ngOnInit(): void {
        this.stream$ = this.store.select<any>('stream');
        this.stream$.subscribe(stream => {
            if (stream && stream.filters) {
                this.filterCount = stream.filters.length
            }
            else {
                this.filterCount = 0;
            }
        })
    }

    removeFilter(id: String) {
        this.store.dispatch(new RemoveFilterAction(id))

    }

    moveFilter(id: String, position: number) {
        this.store.dispatch(new MoveFilterAction(id, position));
    }

    editFilter(id: String) {
        this.store.dispatch(new EditFilterAction(id))
    }

    saveFilter(id: String) {
        this.store.dispatch(new SaveFilterAction(id))
    }

    enableFilter(id: String) {
        this.store.dispatch(new EnableFilterAction(id))
    }

    disableFilter(id: String) {
        this.store.dispatch(new DisableFilterAction(id))
    }
}
