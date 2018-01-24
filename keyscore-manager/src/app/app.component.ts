import {Component, ViewChild, ViewContainerRef} from '@angular/core';
import {Stream} from "./streams/stream.reducer";
import {ModalService} from "./services/modal.service";

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
        <div class="modal fade" id="modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <ng-template #modal></ng-template>
            </div>
        </div>
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

    @ViewChild('modal', {
        read: ViewContainerRef
    }) viewContainerRef: ViewContainerRef;

    constructor(private modalService: ModalService) {

    }

    ngOnInit() {
        this.modalService.setRootViewContainerRef(this.viewContainerRef);
    }
}
