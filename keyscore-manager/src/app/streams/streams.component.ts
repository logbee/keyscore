import {Component} from '@angular/core';
import {select, Store} from "@ngrx/store";
import {Observable} from "rxjs/Observable";
import {StreamState} from "http2";
import {v4 as uuid} from 'uuid'
import {getStreamList, StreamModel} from "./streams.model";
import {CreateStreamAction} from "./streams.actions";
import {Router} from "@angular/router";
import * as RouterActions from '../router/router.actions';

@Component({
    selector: 'keyscore-streams',
    template: `
        <div class="row">
            <div class="col-3">
                <div class="card">
                    <div class="card-body">
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <img class="input-group-text" width="48em" height="48em"
                                     src="/assets/images/magnifying-glass.svg"/>
                            </div>
                            <input type="text" class="form-control" placeholder="search..." aria-label="search">
                        </div>
                        <div class="mt-3 mb-3">
                            <button type="button" class="btn btn-success" (click)="createStream(true)">Create Stream
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-9">
                <div class="card-columns">
                    <div *ngFor="let stream of streams$ | async; let i = index" class="card">
                        <a class="card-header btn d-flex" routerLink="/stream/{{stream.id}}">
                            <h5>{{stream.name}}</h5>
                        </a>
                        <div class="card-body">
                            <small>{{stream.description}}</small>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})
export class StreamsComponent {
    streams$: Observable<StreamModel[]>;

    constructor(private store: Store<StreamState>, private router: Router) {
        this.streams$ = this.store.pipe(select(getStreamList));
    }

    createStream(activeRouting: boolean = false) {
        let streamId = uuid();
        this.store.dispatch(new CreateStreamAction(streamId, "New Stream", ""));
        if (activeRouting) {
            this.store.dispatch(new RouterActions.Go({
                path: ['/stream/' + streamId, {}],
                query: {},
                extras: {}
            }));
        }
    }
}
