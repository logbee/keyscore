import {Component, ViewChild, ViewContainerRef} from '@angular/core';
import {ModalService} from "./services/modal.service";
import {Store} from "@ngrx/store";
import {AppConfig} from "./app.config";
import {TranslateService} from "@ngx-translate/core";

export interface AppState {
    config: AppConfig

}

@Component({
    selector: 'my-app',
    template: `
        <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
            <a class="navbar-brand" href="#" style="text-transform: uppercase">{{'GENERAL.APPNAME' | translate}}</a>
            <div class="navbar-nav">
                <div class="nav-item">
                    <a class="nav-link" routerLink="/dashboard" routerLinkActive="active">{{'GENERAL.DASHBOARD' | translate}}</a>
                </div>
                <div class="nav-item">
                    <a class="nav-link" routerLink="/agent" routerLinkActive="active">{{'APPCOMPONENT.AGENTS' | translate}}</a>
                </div>
                <div class="nav-item">
                    <a class="nav-link" routerLink="/stream" routerLinkActive="active">{{'APPCOMPONENT.STREAMS' | translate}}</a>
                </div>
                <div class="nav-item">
                    <a class="nav-link" routerLink="/filter" routerLinkActive="active">{{'APPCOMPONENT.FILTERS' | translate}}</a>
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

    constructor(store: Store<any>, modalService: ModalService, translate:TranslateService) {
        this.store = store;
        this.modalService = modalService

        translate.setDefaultLang('en');
        translate.use('en');
    }

    ngOnInit() {
        this.modalService.setRootViewContainerRef(this.viewContainerRef);
    }
}
