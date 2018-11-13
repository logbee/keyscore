import {Component, EventEmitter, Input, Output} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: "sidemenu",
    template: `
        <nav fxFill="" fxLayout="column" fxLayoutAlign="space-between start">
            <div>
                <a  routerLink="/dashboard"
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

                    <a *ngIf="isExpanded" mat-list-item routerLink="/resources" routerLinkActive="active">
                        <p matLine>{{'APPCOMPONENT.RESOURCES' | translate}}</p>
                        <mat-icon svgIcon="resources-nav"></mat-icon>
                    </a>

                    <a *ngIf="!isExpanded" mat-list-item routerLink="/resources" routerLinkActive="active"
                       matTooltip="{{'APPCOMPONENT.RESOURCES' | translate}}" matTooltipPosition="right">
                        <mat-icon svgIcon="resources-nav"></mat-icon>
                    </a>

                </mat-nav-list>
                <mat-divider></mat-divider>
            </div>
            <div class="sidebar-footer" fxFlexAlign="stretch">
                <mat-nav-list>
                    <a mat-list-item [matMenuTriggerFor]="languageSelector">
                        <img src="/assets/images/flags/{{translate.currentLang}}.svg"
                             width="24px" height="16px"/>
                        <p matLine *ngIf="translate.currentLang == 'de' && isExpanded">
                            {{'LANGUAGES.GERMAN' | translate}}
                        </p>
                        <p matLine="" *ngIf="translate.currentLang == 'en' && isExpanded">
                            {{'LANGUAGES.ENGLISH' | translate}}
                        </p>
                    </a>
                    <mat-menu xPosition="before" #languageSelector="matMenu">
                        <button mat-menu-item (click)="setLanguage('de')">
                            <img class="mr-2" src="/assets/images/flags/de.svg" width="24px" height="16px">
                            {{'LANGUAGES.GERMAN' | translate}}
                        </button>
                        <button mat-menu-item (click)="setLanguage('en')">
                            <img class="mr-2" src="/assets/images/flags/en.svg" width="24px" height="16px">
                            {{'LANGUAGES.ENGLISH' | translate}}
                        </button>
                    </mat-menu>

                    <a *ngIf="showSettings" mat-list-item routerLink="/settings" routerLinkActive="active">
                        <mat-icon>settings</mat-icon>
                        <p matLine *ngIf="isExpanded">
                            {{'SETTINGS.TITLE' | translate}}
                        </p>
                    </a>

                    <a mat-list-item (click)="toggleMenu()">
                        <p matLine *ngIf="isExpanded">{{'GENERAL.COLLAPSE' | translate}}</p>
                        <mat-icon *ngIf="!isExpanded" svgIcon="expand-nav"></mat-icon>
                        <mat-icon *ngIf="isExpanded" svgIcon="collapse-nav"></mat-icon>
                    </a>
                    
                </mat-nav-list>
            </div>
        </nav>`
})


export class SidemenuComponent {
    @Input() public showSettings: boolean = true;
    @Output() public toggleSidebar: EventEmitter<void> = new EventEmitter();
    @Output() public updateLanguage: EventEmitter<string> = new EventEmitter();

    public sideBarClassName: string = "";
    public isExpanded: boolean = true;

    constructor(private translate: TranslateService) {

    }

    public toggleMenu() {
        this.isExpanded = !this.isExpanded;
        this.toggleSidebar.emit();
    }

    public setLanguage(lang: string) {
        this.updateLanguage.emit(lang);
    }
}
