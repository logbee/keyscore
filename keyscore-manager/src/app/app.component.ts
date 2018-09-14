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

export interface AppState {
    config: AppConfig;
    settings: SettingsState;
    spinner: LoadingState;
    menu: MenuState;
    error: ErrorState;
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

    constructor(private store: Store<any>, modalService: ModalService, private translate: TranslateService) {
        this.store = store;
        this.modalService = modalService;
        this.store.select(selectAppConfig).subscribe((conf) => {
            this.settingsFeatureEnabled = conf.getBoolean("keyscore.manager.features.settings");
        });
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
}
