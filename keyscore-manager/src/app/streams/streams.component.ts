import {Component} from '@angular/core';
import {select, Store} from "@ngrx/store";
import {Observable} from "rxjs/Observable";
import {StreamState} from "http2";
import {v4 as uuid} from 'uuid'
import {getStreamList, StreamModel} from "./streams.model";
import {CreateStreamAction} from "./streams.actions";

@Component({
    selector: 'keyscore-streams',
    template: require('./streams.component.html')
})
export class StreamsComponent {
    streams$: Observable<StreamModel[]>;

    constructor(private store: Store<StreamState>) {
        this.streams$ = this.store.pipe(select(getStreamList));
    }

    createStream() {
        this.store.dispatch(new CreateStreamAction(uuid(), "My Stream", ""));
    }
}
