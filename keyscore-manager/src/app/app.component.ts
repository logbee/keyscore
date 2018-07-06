import {Component, ViewChild, ViewContainerRef} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {AppConfig} from "./app.config";
import * as fromSpinner from "./loading/loading.reducer";
import {LoadFilterDescriptorsAction} from "./pipelines/pipelines.actions";
import {ModalService} from "./services/modal.service";
import "./style/style.css";

export interface AppState {
    config: AppConfig;
    spinner: fromSpinner.State;

}

@Component({
    selector: "my-app",
    template: `
        <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
            <a class="navbar-brand" href="#" style="text-transform: uppercase">{{'GENERAL.APPNAME' | translate}}</a>
            <ul class="navbar-nav mr-auto">
                <li class="nav-item">
                    <a class="nav-link" routerLink="/dashboard"
                       routerLinkActive="active">{{'GENERAL.DASHBOARD' | translate}}</a>
                </li>
                <li class="nav-item">
                    <a id ="test" class="nav-link" routerLink="/agent"
                       routerLinkActive="active">{{'APPCOMPONENT.AGENTS' | translate}}</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" routerLink="/pipelines/pipeline"
                       routerLinkActive="active">{{'APPCOMPONENT.PIPELINES' | translate}}</a>
                </li>
            </ul>
            <ul class="navbar-nav ml-auto">
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" id="navbardrop" data-toggle="dropdown">
                        <img  class="mr-2" src="/assets/images/flags/{{translate.currentLang}}.svg"
                              width="24px" height="16px"/>
                    </a>
                    <div class="dropdown-menu dropdown-menu-right">
                        <span class="dropdown-item" style="{cursor: pointer;}" (click)="setLanguage('de')">
                            <img  class="mr-2" src="/assets/images/flags/de.svg" width="24px" height="16px" >
                            {{'LANGUAGES.GERMAN' | translate}}
                        </span>
                        <span class="dropdown-item" style="{cursor: pointer;}" (click)="setLanguage('en')">
                            <img class="mr-2" src="/assets/images/flags/en.svg" width="24px" height="16px" >
                            {{'LANGUAGES.ENGLISH' | translate}}
                        </span>
                    </div>
                </li>
            </ul>
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

    @ViewChild("modal", {
        read: ViewContainerRef
    }) public viewContainerRef: ViewContainerRef;

    private modalService: ModalService;
    private store: Store<any>;

    constructor(store: Store<any>, modalService: ModalService, private translate: TranslateService) {
        this.store = store;
        this.modalService = modalService;
    }

    public ngOnInit() {
        this.modalService.setRootViewContainerRef(this.viewContainerRef);
    }

    public setLanguage(language: string) {
        this.translate.use(language);
        this.store.dispatch(new LoadFilterDescriptorsAction());
        this.translate.use(language);
    }

}
