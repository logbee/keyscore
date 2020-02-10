import {HttpClient, HttpClientModule} from "@angular/common/http";
import {APP_INITIALIZER, NgModule} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterModule, Routes} from "@angular/router";

import {StoreModule} from "@ngrx/store";
import {StoreRouterConnectingModule} from "@ngrx/router-store";
import {StoreDevtoolsModule} from "@ngrx/store-devtools";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {AgentsModule} from "./agents/agents.module";
import {AppComponent} from "./app.component";
import {AppConfigEffects, AppConfigLoader, initializer, KeycloakConfigLoader} from "./app.config";
import {reducers} from "./app.reducers";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {metaReducers} from "./meta.reducers";
import {PipelinesModule} from "./pipelines/pipelines.module";
import {RouterEffects} from "./router/router.effects";
import {SettingsComponent} from "./settings/settings.component";
import {HeaderBarModule} from "./common/headerbar/headerbar.module";
import {SidemenuComponent} from "./common/sidemenu/sidemenu.component";
import {LoadingEffects} from "./common/loading/loading.effects";
import {ErrorEffects} from "./common/error/error.effects";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {ResourcesModule} from "./resources/resources.module";
import {EffectsModule} from "@ngrx/effects";
import {SnackbarEffects} from "./common/snackbar/snackbar.effects";
import {DataSourceFactory} from "./data-source/data-source-factory";
import {KeycloakAngularModule, KeycloakService} from "keycloak-angular";
import {AppAuthGuard} from "./app.authguard";

import "../assets/styles.scss";
import "../assets/global-table-styles.scss";
import "../assets/pipely-style.scss";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";
import {IconModule} from "./icon.module"
import {UnauthorizedComponent} from "@/app/common/unauthorized/unauthorized.component";

let routes: Routes = [
    {path: "", redirectTo: "/dashboard", pathMatch: "full"},
    {path: "dashboard", component: DashboardComponent, canActivate:[AppAuthGuard]},
    {path: "agent", loadChildren: () => AgentsModule},
    {path: "pipelines", loadChildren: () => PipelinesModule},
    {path: "settings", component: SettingsComponent, canActivate:[AppAuthGuard]},
    {path: "unauthorized", component: UnauthorizedComponent}
];

export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http);
}

@NgModule({
    imports: [
        BrowserAnimationsModule,
        FormsModule,
        HttpClientModule,
        ReactiveFormsModule,
        RouterModule.forRoot(routes),
        StoreModule.forRoot(reducers, {metaReducers}),
        EffectsModule.forRoot([AppConfigEffects, RouterEffects, LoadingEffects, ErrorEffects, SnackbarEffects]),
        StoreRouterConnectingModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [HttpClient]
            }
        }),
        // ToDO: Throws DataCloneError
        StoreDevtoolsModule.instrument({
            maxAge: 20
        }),
        HeaderBarModule,
        MaterialModule,
        KeycloakAngularModule,
        IconModule
    ],
    declarations: [
        AppComponent,
        DashboardComponent,
        SettingsComponent,
        SidemenuComponent,
        UnauthorizedComponent
    ],
    providers: [
        AppConfigLoader,
        KeycloakConfigLoader,
        AppAuthGuard,
        {
            provide: APP_INITIALIZER,
            useFactory: initializer,
            deps: [AppConfigLoader, KeycloakConfigLoader, KeycloakService],
            multi: true
        },
        DataSourceFactory,

    ],
    entryComponents: [
        SettingsComponent,
        UnauthorizedComponent
    ],
    bootstrap: [
        AppComponent
    ]
})
export class AppModule {
}
