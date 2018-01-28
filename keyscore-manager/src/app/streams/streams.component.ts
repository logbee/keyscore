import {Component} from '@angular/core';

class StreamsModel {
    streams: StreamModel[]
}

class StreamModel {
    id: string;
    name: string;
    description: string;
}

@Component({
    selector: 'keyscore-streams',
    template: `
        <div class="row">
            <div class="col-3">
                <div class="card">
                    <div class="card-body">
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <img class="input-group-text" width="48em" src="/assets/images/magnifying-glass.svg"/>
                            </div>
                            <input type="text" class="form-control" placeholder="search..." aria-label="search">
                        </div>
                        <div class="mt-3 mb-3">
                            <button type="button" class="btn btn-success" routerLink="/stream/new">Create Stream
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-9">
                <div class="card-columns">
                    <div *ngFor="let stream of model.streams; let i = index" class="card">
                        <a class="card-header btn d-flex" routerLink="/stream/{{stream.id}}">
                            <h4 class="">{{stream.name}}</h4>
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
    model: StreamsModel = {
        streams: [
            { id: '1', name: 'Stream 1', description: 'A simple test Stream.' },
            { id: '2', name: 'MyStream', description: 'A stream from A to B.' },
            { id: '3', name: 'The Stream', description: 'The best Stream!' },
            { id: '4', name: 'Another Stream', description: 'Just another stream.' },
            { id: '5', name: 'Old Stream', description: 'Backup stream.' },
        ]
    }
}
