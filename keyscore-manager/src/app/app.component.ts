import { Component } from '@angular/core';
import {FilterInstance, Stream} from "./streams/stream.reducer";

export interface AppState {
    stream: Stream;
}

@Component({
    selector: 'my-app',
    template: `
        <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
            <a class="navbar-brand" href="#">KEYSCORE</a>
            <div class="navbar-nav">
                <div class="nav-item">
                    <a class="nav-link" routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
                </div>
                <div class="nav-item">
                    <a class="nav-link" routerLink="/node" routerLinkActive="active">Agents</a>
                </div>
                <div class="nav-item">
                    <a class="nav-link" routerLink="/stream" routerLinkActive="active">Streams</a>
                </div>
                <div class="nav-item">
                    <a class="nav-link" routerLink="/filter" routerLinkActive="active">Filters</a>
                </div>
            </div>
        </nav>
        <div class="container-fluid">
            <div class="row">
                <div class="col-12 pb-5 mt-3">
                    <router-outlet></router-outlet>
                </div>
            </div>
        </div>
    `
})

export class AppComponent {
}
