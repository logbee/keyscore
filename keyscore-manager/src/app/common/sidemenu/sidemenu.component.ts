import {Component, EventEmitter, Input, Output} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";
import {KeycloakService} from "keycloak-angular";
import {Store} from "@ngrx/store";
import {AppState} from "@/app/app.component";

@Component({
    selector: "sidemenu",
    template: `
        <nav fxFill="" fxLayout="column" fxLayoutAlign="space-between start">
            <div>
                <a routerLink="/dashboard"
                   routerLinkActive="active">
                    <div class="sidebar-header">
                        <img *ngIf="isExpanded" src="/assets/images/logos/svg/dark/keyscore.font.dark.svg">
                        <strong *ngIf="!isExpanded">KS</strong>
                    </div>
                </a>

                <mat-nav-list>

                    <a *ngIf="isExpanded" mat-list-item routerLink="/dashboard" routerLinkActive="active">
                        <p matLine>{{'APPCOMPONENT.DASHBOARD' | translate}}</p>
                        <mat-icon svgIcon="dashboard-nav"></mat-icon>
                    </a>

                    <a *ngIf="!isExpanded" mat-list-item routerLink="/dashboard" routerLinkActive="active"
                       matTooltip="{{'APPCOMPONENT.DASHBOARD' | translate}}" matTooltipPosition="right">
                        <mat-icon svgIcon="dashboard-nav"></mat-icon>
                    </a>

                    <a *ngIf="isExpanded" mat-list-item routerLink="/agent" routerLinkActive="active">
                        <p matLine>{{'APPCOMPONENT.AGENTS' | translate}}</p>
                        <mat-icon svgIcon="agents-nav"></mat-icon>
                    </a>

                    <a *ngIf="!isExpanded" mat-list-item routerLink="/agent" routerLinkActive="active"
                       matTooltip="{{'APPCOMPONENT.AGENTS' | translate}}" matTooltipPosition="right">
                        <mat-icon svgIcon="agents-nav"></mat-icon>
                    </a>

                    <a *ngIf="isExpanded" mat-list-item routerLink="/pipelines" routerLinkActive="active">
                        <p matLine>{{'APPCOMPONENT.PIPELINES' | translate}}</p>
                        <mat-icon svgIcon="pipelines-nav"></mat-icon>
                    </a>

                    <a *ngIf="!isExpanded" mat-list-item routerLink="/pipelines" routerLinkActive="active"
                       matTooltip="{{'APPCOMPONENT.PIPELINES' | translate}}" matTooltipPosition="right">
                        <mat-icon svgIcon="pipelines-nav"></mat-icon>
                    </a>

                    <a *ngIf="isExpanded" mat-list-item href="/doc/index.html" target="_blank">
                        <p matLine>{{'APPCOMPONENT.DOCUMENTATION' | translate}}</p>
                        <mat-icon svgIcon="documentation-nav"></mat-icon>
                    </a>

                    <a *ngIf="!isExpanded" mat-list-item href="/doc/index.html" target="_blank"
                       matTooltip="{{'APPCOMPONENT.DOCUMENTATION' | translate}}" matTooltipPosition="right">
                        <mat-icon svgIcon="documentation-nav"></mat-icon>
                    </a>

                </mat-nav-list>
                <mat-divider></mat-divider>
            </div>
            <div class="sidebar-footer" fxFlexAlign="stretch">
                <mat-nav-list>
                    <a class="no-link" *ngIf="isLoggedIn" mat-list-item [matTooltip]="!isExpanded ? userName : null"
                       matTooltipPosition="right">
                        <p mat-line *ngIf="isExpanded">{{userName}}</p>
                        <mat-icon>account_box</mat-icon>
                    </a>
                    <a *ngIf="isLoggedIn" mat-list-item (click)="keycloak.logout()">
                        <p mat-line *ngIf="isExpanded">{{'GENERAL.LOGOUT' | translate}}</p>
                        <mat-icon>exit_to_app</mat-icon>
                    </a>
                    <a mat-list-item [matMenuTriggerFor]="languageSelector">
                        <mat-icon [svgIcon]="buildCurrentLanguageSvgString()"></mat-icon>
                        <p matLine *ngIf="translate.currentLang == 'de' && isExpanded">
                            {{'LANGUAGES.GERMAN' | translate}}
                        </p>
                        <p matLine="" *ngIf="translate.currentLang == 'en' && isExpanded">
                            {{'LANGUAGES.ENGLISH' | translate}}
                        </p>
                    </a>
                    <mat-menu xPosition="before" #languageSelector="matMenu">
                        <button mat-menu-item (click)="setLanguage('de')">
                            <mat-icon svgIcon="lang-de"></mat-icon>
                            {{'LANGUAGES.GERMAN' | translate}}
                        </button>
                        <button mat-menu-item (click)="setLanguage('en')">
                            <mat-icon svgIcon="lang-en"></mat-icon>
                            {{'LANGUAGES.ENGLISH' | translate}}
                        </button>
                    </mat-menu>


                    <a mat-list-item (click)="toggleMenu()">
                        <p matLine *ngIf="isExpanded">{{'GENERAL.COLLAPSE' | translate}}</p>
                        <mat-icon *ngIf="!isExpanded">keyboard_arrow_right</mat-icon>
                        <mat-icon *ngIf="isExpanded">keyboard_arrow_left</mat-icon>
                    </a>

                </mat-nav-list>
            </div>
        </nav>`
})


export class SidemenuComponent {
    @Output() public toggleSidebar: EventEmitter<void> = new EventEmitter();
    @Output() public updateLanguage: EventEmitter<string> = new EventEmitter();

    public sideBarClassName: string = "";
    public isExpanded: boolean = true;
    public isLoggedIn: boolean = false;
    public userName: string = "";

    constructor(private translate: TranslateService, private keycloak: KeycloakService) {
        this.checkIsLoggedIn();
    }

    private async checkIsLoggedIn() {
        this.isLoggedIn = await this.keycloak.isLoggedIn();
        if (this.isLoggedIn) {
            this.userName = this.keycloak.getUsername();
        }
    }

    public toggleMenu() {
        this.isExpanded = !this.isExpanded;
        this.toggleSidebar.emit();
    }

    public setLanguage(lang: string) {
        this.updateLanguage.emit(lang);
    }

    public buildCurrentLanguageSvgString() {
        return 'lang-' + this.translate.currentLang;
    }
}
