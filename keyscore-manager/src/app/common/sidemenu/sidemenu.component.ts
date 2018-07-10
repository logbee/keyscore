import {Component, EventEmitter, Output} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: "sidemenu",
    template: `
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
                        <span *ngIf="isSideBarExpanded()">
                                {{'GENERAL.DASHBOARD' | translate}}
                            </span>
                    </a>
                </li>
                <li>
                    <a id="test" routerLink="/agent"
                       routerLinkActive="active">
                        <span><img src="/assets/images/menu/worker.png"></span>
                        <span *ngIf="isSideBarExpanded()">
                                {{'APPCOMPONENT.AGENTS' | translate}}
                            </span>
                    </a>
                </li>
                <li>
                    <a class="nav-link" routerLink="/pipelines/pipeline"
                       routerLinkActive="active">
                        <span><img src="/assets/images/menu/sitemap.png"></span>
                        <span *ngIf="isSideBarExpanded()">
                                {{'APPCOMPONENT.PIPELINES' | translate}}
                            </span>
                    </a>
                </li>
            </ul>
            <div class="sidebar-footer">
                <ul class="list-unstyled components">
                    <li class="nav-item dropdown" id="language-selector">
                        <a class="nav-link dropdown-toggle" href="#" id="navbardrop" data-toggle="dropdown">
                            <img src="/assets/images/flags/{{translate.currentLang}}.svg"
                                 width="24px" height="16px"/>
                            <span *ngIf="translate.currentLang == 'de' && isSideBarExpanded()">
                                    {{'LANGUAGES.GERMAN' | translate}}
                                </span>
                            <span *ngIf="translate.currentLang == 'en' && isSideBarExpanded()">
                                    {{'LANGUAGES.ENGLISH' | translate}}
                                </span>
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
                    <li *ngIf="settingsFeatureEnabled">
                        <a class="nav-link" routerLink="/settings" routerLinkActive="active">
                                <span>
                                    <img src="/assets/images/ic_settings_white_24px.svg" width="24px" height="24px"/>
                                </span>
                            <span *ngIf="isSideBarExpanded()">
                                    {{'SETTINGS.TITLE' | translate}}
                                </span>
                        </a>
                    </li>
                    <li>
                        <a (click)="toggleMenu()">
                            <span class="hide-on-collapse"><img
                                    src="/assets/images/menu/arrow-left-drop-circle.png"></span>
                            <span class="hide-on-expand"><img
                                    src="/assets/images/menu/arrow-right-drop-circle.png"></span>
                            <span class="hide-on-collapse">{{'GENERAL.COLLAPSE' | translate}}</span>
                        </a>
                    </li>
                </ul>
            </div>
        </nav>`
})

export class SidemenuComponent {
    @Output() public toggleSidebar: EventEmitter<void> = new EventEmitter();
    @Output() public updateLanguage: EventEmitter<string> = new EventEmitter();

    public sideBarClassName: string = "";

    constructor(private translate: TranslateService) {
    }

    public toggleMenu() {
        this.sideBarClassName = this.sideBarClassName === "" ? "active" : "";
        this.toggleSidebar.emit();
    }

    public setLanguage(lang: string) {
        this.updateLanguage.emit(lang);
    }

    public isSideBarExpanded() {
        return this.sideBarClassName === "";
    }

}
