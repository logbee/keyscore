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
                    
                    <a mat-list-item routerLink="/dashboard" routerLinkActive="active">
                        <p matLine *ngIf="isExpanded">{{'GENERAL.DASHBOARD' | translate}}</p>
                        <mat-icon *ngIf="isExpanded" svgIcon="dashboard-nav"></mat-icon>
                        <mat-icon *ngIf="isCollapsed" svgIcon="dashboard-nav" matTooltip="Dashboard" matTooltipPosition="right"></mat-icon>
                    </a>

                    <a mat-list-item routerLink="/agent" routerLinkActive="active">
                        <p matLine *ngIf="isExpanded">{{'APPCOMPONENT.AGENTS' | translate}}</p>
                        <mat-icon *ngIf="isExpanded" svgIcon="agents-nav"></mat-icon>
                        <mat-icon *ngIf="isCollapsed" svgIcon="agents-nav" matTooltip="Agents" matTooltipPosition="right"></mat-icon>
                    </a>

                    <a mat-list-item routerLink="/pipelines" routerLinkActive="active">
                        <p matLine *ngIf="isExpanded">{{'APPCOMPONENT.PIPELINES' | translate}}</p>
                        <mat-icon *ngIf="isExpanded" svgIcon="pipelines-nav"></mat-icon>
                        <mat-icon *ngIf="isCollapsed" svgIcon="pipelines-nav" matTooltip="Pipelines" matTooltipPosition="right"></mat-icon>
                    </a>
                    
                    <a mat-list-item routerLink="/resources" routerLinkActive="active">
                        <p matLine *ngIf="isExpanded">{{'APPCOMPONENT.RESOURCES' | translate}}</p>
                        <mat-icon *ngIf="isExpanded" svgIcon="resources-nav"></mat-icon>
                        <mat-icon *ngIf="isCollapsed" svgIcon="resources-nav" matTooltip="Resources" matTooltipPosition="right"></mat-icon>
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
                        <mat-icon *ngIf="isExpanded">keyboard_arrow_left</mat-icon>
                        <mat-icon *ngIf="!isExpanded">keyboard_arrow_right</mat-icon>
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
    public isCollapsed: boolean = false;

    constructor(private translate: TranslateService) {

    }

    public toggleMenu() {
        this.isExpanded = !this.isExpanded;
        this.isCollapsed = !this.isCollapsed;
        this.toggleSidebar.emit();
    }

    public setLanguage(lang: string) {
        this.updateLanguage.emit(lang);
    }
}
