import {Component, ViewChild, ViewContainerRef} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {AppConfig} from "./app.config";
import * as fromSpinner from "./loading/loading.reducer";
import {LoadFilterDescriptorsAction} from "./pipelines/pipelines.actions";
import {ModalService} from "./services/modal.service";
import "./style/style.css";
import {SettingsComponent} from "./settings/settings.component";
import {SettingsState} from "./settings/settings.model";

export interface AppState {
    config: AppConfig;
    settings: SettingsState;
    spinner: fromSpinner.State;
}

@Component({
    selector: "my-app",
    template: `
        <div class="wrapper">
            <nav id="sidebar" [class]="sideBarClassName">
                <div class="sidebar-header">
                    <img src="/assets/images/logos/keyscore-header.dark.svg">
                    <strong>KS</strong>
                </div>

                <ul class="list-unstyled components">
                    <li>
                        <a routerLink="/dashboard"
                           routerLinkActive="active">
                            <span><img src="/assets/images/menu/desktop-mac-dashboard.png"></span>
                            {{'GENERAL.DASHBOARD' | translate}}
                        </a>
                    </li>
                    <li>
                        <a id="test" routerLink="/agent"
                           routerLinkActive="active">
                            <span><img src="/assets/images/menu/worker.png"></span>
                            {{'APPCOMPONENT.AGENTS' | translate}}</a>
                    </li>
                    <li>
                        <a class="nav-link" routerLink="/pipelines/pipeline"
                           routerLinkActive="active">
                            <span><img src="/assets/images/menu/sitemap.png"></span>
                            {{'APPCOMPONENT.PIPELINES' | translate}}</a>
                    </li>

                </ul>

                <div class="sidebar-footer">
                    <ul class="list-unstyled components">

                        <li>
                            <a (click)="showSettings()">
                                <span>
                                    <img src="/assets/images/ic_settings_white_24px.svg" width="24px" height="24px"/>
                                </span>
                                {{'SETTINGS.DIALOG_TITLE' | translate}}
                            </a>
                        </li>
                        <li class="nav-item dropdown" id="language-selector">
                            <a class="nav-link dropdown-toggle" href="#" id="navbardrop" data-toggle="dropdown">
                                <img src="/assets/images/flags/{{translate.currentLang}}.svg"
                                     width="24px" height="16px"/><span
                                    *ngIf="translate.currentLang == 'de'">{{'LANGUAGES.GERMAN' | translate}}</span>
                                <span *ngIf="translate.currentLang == 'en'">{{'LANGUAGES.ENGLISH' | translate}}</span>
                            </a>
                            <div class="dropdown-menu">
                            <span class="dropdown-item" style="{cursor: pointer;}" (click)="setLanguage('de')">
                                <img class="mr-2" src="/assets/images/flags/de.svg" width="24px" height="16px">
                                {{'LANGUAGES.GERMAN' | translate}}
                            </span>
                                <span class="dropdown-item" style="{cursor: pointer;}" (click)="setLanguage('en')">
                                <img class="mr-2" src="/assets/images/flags/en.svg" width="24px" height="16px">
                                {{'LANGUAGES.ENGLISH' | translate}}
                            </span>
                            </div>
                        </li>
                        <li>
                            <a (click)="toggleMenu()">
                            <span class="hide-on-collapse"><img
                                    src="/assets/images/menu/arrow-left-drop-circle.png"></span>
                                <span class="hide-on-expand"><img
                                        src="/assets/images/menu/arrow-right-drop-circle.png"></span>
                                <span class="hide-on-collapse">{{'GENERAL.COLLAPSE' | translate}}</span>
                                <span class="hide-on-expand">{{'GENERAL.EXPAND' | translate}}</span>
                            </a>
                        </li>

                    </ul>
                </div>

            </nav>

            <!--<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
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
                <ul class="navbar-nav">
                    <li class="nav-item">
                        <button type="button" class="nav-link btn btn-primary" (click)="showSettings()">
                            <img src="/assets/images/ic_settings_white_24px.svg"
                                 width="24px" height="24px"/>
                        </button>
                    </li>
                </ul>
            </nav>-->
            <div id="modal">
                <ng-template #modal></ng-template>
            </div>
            <div class="container-fluid">
                <div class="row">
                    <div class="col-12 pb-5 mt-1">
                        <router-outlet></router-outlet>
                    </div>
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

    public sideBarClassName: string;

    private modalService: ModalService;
    private store: Store<any>;

    constructor(store: Store<any>, modalService: ModalService, private translate: TranslateService) {
        this.store = store;
        this.modalService = modalService;
        this.sideBarClassName = "";
    }

    public ngOnInit() {
        this.modalService.setRootViewContainerRef(this.viewContainerRef);
    }

    public setLanguage(language: string) {
        this.translate.use(language);
        this.store.dispatch(new LoadFilterDescriptorsAction());
        this.translate.use(language);
    }

    public toggleMenu() {
        this.sideBarClassName = this.sideBarClassName === "" ? "active" : "";
    }

    protected showSettings() {
        this.modalService.show(SettingsComponent)
    }
}
