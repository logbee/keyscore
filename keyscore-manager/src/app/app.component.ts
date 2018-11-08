import {Component, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {AppConfig, selectAppConfig} from "./app.config";
import {LoadFilterDescriptorsAction} from "./pipelines/pipelines.actions";
import {ModalService} from "./services/modal.service";
import "./style/style.css";
import {SettingsComponent} from "./settings/settings.component";
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
                    <sidemenu [showSettings]="settingsFeatureEnabled" (toggleSidebar)="toggleMenu()"
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
        Store,
        ModalService
    ]
})

export class AppComponent implements OnInit {

    @ViewChild("modal", {
        read: ViewContainerRef
    }) public viewContainerRef: ViewContainerRef;

    private modalService: ModalService;
    private settingsFeatureEnabled: boolean;

    constructor(private store: Store<any>,
                 modalService: ModalService,
                private translate: TranslateService,
                private matIconRegistry: MatIconRegistry,
                private domSanitizer: DomSanitizer) {
        this.store = store;
        this.modalService = modalService;
        this.store.select(selectAppConfig).subscribe((conf) => {
            this.settingsFeatureEnabled = conf.getBoolean("keyscore.manager.features.settings");
        });

        this.addCustomIcons();
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
        this.store.dispatch(new ToggleMenuAction());
    }

    protected showSettings() {
        this.modalService.show(SettingsComponent);
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
        this.matIconRegistry.addSvgIcon('navigate-to-pipely', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/navigation/pipely-navigation.svg"))
    }
}