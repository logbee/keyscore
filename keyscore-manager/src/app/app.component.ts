import {Component, ViewChild, ViewContainerRef} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {AppConfig} from "./app.config";
import {LoadFilterDescriptorsAction} from "./pipelines/pipelines.actions";
import "./style/style.scss";
import {SettingsState} from "./settings/settings.model";
import {MenuState} from "./common/sidemenu/sidemenu.reducer";
import {LoadingState} from "./common/loading/loading.reducer";
import {ToggleMenuAction} from "./common/sidemenu/sidemenu.actions";
import {ErrorState} from "./common/error/error.reducer";
import {SnackbarState} from "./common/snackbar/snackbar.reducer";
import {DomSanitizer} from "@angular/platform-browser";
import {MatIconRegistry} from "@angular/material";

export interface AppState {
    config: AppConfig;
    settings: SettingsState;
    spinner: LoadingState;
    menu: MenuState;
    error: ErrorState;
    snackbar: SnackbarState;
}

@Component({
    selector: "my-app",
    template: `
        <div class="app-container">
            <mat-sidenav-container class="sidenav-container" autosize>
                <mat-sidenav mode="side" class="main-drawer" opened="true">
                    <sidemenu (toggleSidebar)="toggleMenu()"
                              (updateLanguage)="setLanguage($event)"></sidemenu>
                </mat-sidenav>

                <div id="modal">
                    <ng-template #modal></ng-template>
                </div>
                <mat-sidenav-content class="sidenav-content">
                    <router-outlet></router-outlet>
                </mat-sidenav-content>

            </mat-sidenav-container>
        </div>
    `,
    providers: [
        Store
    ]
})

export class AppComponent {

    @ViewChild("modal", {
        read: ViewContainerRef
    }) public viewContainerRef: ViewContainerRef;


    constructor(private store: Store<any>,
                private translate: TranslateService,
                private matIconRegistry: MatIconRegistry,
                private domSanitizer: DomSanitizer) {
        this.store = store;


        this.addCustomIcons();
    }

    public setLanguage(language: string) {
        this.translate.use(language);
        this.store.dispatch(new LoadFilterDescriptorsAction());
        this.translate.use(language);
    }

    public toggleMenu() {
        this.store.dispatch(new ToggleMenuAction());
    }

    private addCustomIcons(){
        // Custom stage icons
        this.matIconRegistry.addSvgIcon('source-stage', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/stages/source-stage.svg"));
        this.matIconRegistry.addSvgIcon('sink-stage', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/stages/sink-stage.svg"));
        this.matIconRegistry.addSvgIcon('filter-stage', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/stages/filter-stage.svg"));
        this.matIconRegistry.addSvgIcon('merge-stage', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/stages/merge-block.svg"));
        this.matIconRegistry.addSvgIcon('branch-stage', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/stages/branch-block.svg"));

        // custom data icons
        this.matIconRegistry.addSvgIcon('text-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/text-value.svg"));
        this.matIconRegistry.addSvgIcon('boolean-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/boolean-value.svg"));
        this.matIconRegistry.addSvgIcon('decimal-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/decimal-value.svg"));
        this.matIconRegistry.addSvgIcon('duration-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/duration-value.svg"));
        this.matIconRegistry.addSvgIcon('number-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/number-value.svg"));
        this.matIconRegistry.addSvgIcon('timestamp-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/timestamp-value.svg"));

        // Custom Naviagation Icons
        this.matIconRegistry.addSvgIcon('navigate-to-pipely', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/navigation/pipely-navigation.svg"));

        this.matIconRegistry.addSvgIcon('pipelines-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/pipeline-nav.svg'));
        this.matIconRegistry.addSvgIcon('agents-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/menu/agents.svg'));
        this.matIconRegistry.addSvgIcon('dashboard-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/speedometer.svg'));
        this.matIconRegistry.addSvgIcon('resources-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/menu/resources.svg'));
        this.matIconRegistry.addSvgIcon('expand-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/chevron-right-round.svg'));
        this.matIconRegistry.addSvgIcon('collapse-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/chevron-left-round.svg'));
    }
}