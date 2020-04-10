import {Component, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {AppConfig, selectAppConfig} from "./app.config";
import {LoadFilterDescriptorsAction} from "./pipelines/actions/pipelines.actions";
import {SettingsState} from "./settings/settings.model";
import {MenuState} from "./common/sidemenu/sidemenu.reducer";
import {LoadingState} from "./common/loading/loading.reducer";
import {ToggleMenuAction} from "./common/sidemenu/sidemenu.actions";
import {ErrorState} from "./common/error/error.reducer";
import {SnackbarState} from "./common/snackbar/snackbar.reducer";
import {DomSanitizer} from "@angular/platform-browser";
import {MatIconRegistry} from "@angular/material/icon";
import {AgentsState} from "@/app/agents/agents.reducer";
import {Router} from "@angular/router";
import {take} from "rxjs/operators";

export interface AppState {
    config: AppConfig;
    settings: SettingsState;
    spinner: LoadingState;
    menu: MenuState;
    error: ErrorState;
    snackbar: SnackbarState;
    agents: AgentsState;
}


@Component({
    selector: "my-app",
    template: `
        <div class="app-container">
            <mat-sidenav-container class="sidenav-container" autosize>
                <mat-sidenav mode="side" class="main-drawer" opened="true">
                    <sidemenu (toggleSidebar)="toggleMenu()"
                              (updateLanguage)="setLanguage($event)"
                    ></sidemenu>
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

export class AppComponent implements OnInit {

    @ViewChild("modal", {
        read: ViewContainerRef,
        static: true
    }) public viewContainerRef: ViewContainerRef;

    constructor(private store: Store<any>,
                private translate: TranslateService,
                private matIconRegistry: MatIconRegistry,
                private domSanitizer: DomSanitizer,
                private router: Router) {

        this.addPermittedRolesToRoutes();


    }

    ngOnInit(): void {

    }

    public setLanguage(language: string) {
        this.translate.use(language);
        this.store.dispatch(new LoadFilterDescriptorsAction());
        this.translate.use(language);
    }

    public toggleMenu() {
        this.store.dispatch(new ToggleMenuAction());
    }

    private addPermittedRolesToRoutes() {
        let roles: string[];
        this.store.pipe(select(selectAppConfig), take(1)).subscribe(conf =>
            roles = conf.getArray("keyscore.keycloak.roles")
        );

        const data = {roles: roles};

        this.router.config.forEach(route => {
            route.data = data;
        });
    }
}
