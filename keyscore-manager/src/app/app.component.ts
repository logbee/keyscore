import {Component, ViewChild, ViewContainerRef} from '@angular/core';
import {Stream} from "./streams/stream.reducer";
import {ModalService} from "./services/modal.service";
import {Store} from "@ngrx/store";
import {FilterDescriptor} from "./services/filter.service";
import {AppConfig} from "./app.config";

export interface AppState {
    config: AppConfig
    stream: Stream;
    filterDescriptors: FilterDescriptor[];
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
        <div id="modal">
            <ng-template #modal></ng-template>
        </div>
        <div class="container-fluid">
            <div class="row">
                <div class="col-12 pb-5 mt-3">
                    <router-outlet></router-outlet>
                </div>
            </div>
        </div>
    `,
    providers: [
        Store,
        ModalService
    ]
})

export class AppComponent {

    @ViewChild('modal', {
        read: ViewContainerRef
    }) viewContainerRef: ViewContainerRef;

    private modalService: ModalService;
    private store: Store<any>;

    constructor(store: Store<any>, modalService: ModalService) {
        this.store = store;
        this.modalService = modalService
    }

    ngOnInit() {
        this.modalService.setRootViewContainerRef(this.viewContainerRef);
    }
}
