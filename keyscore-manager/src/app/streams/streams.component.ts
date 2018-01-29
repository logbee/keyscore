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
    template: require('./streams.component.html')
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
