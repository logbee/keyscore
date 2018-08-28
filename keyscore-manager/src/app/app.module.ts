import {HttpClient, HttpClientModule} from "@angular/common/http";
import {APP_INITIALIZER, NgModule} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BrowserModule} from "@angular/platform-browser";
import {RouterModule, Routes} from "@angular/router";

import {StoreModule} from "@ngrx/store";

import {EffectsModule} from "@ngrx/effects";
import {StoreRouterConnectingModule} from "@ngrx/router-store";
import {StoreDevtoolsModule} from "@ngrx/store-devtools";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {AgentsModule} from "./agents/agents.module";
import {AppComponent} from "./app.component";
import {AppConfigEffects, AppConfigLoader} from "./app.config";
import {reducers} from "./app.reducers";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {metaReducers} from "./meta.reducers";
import {FilterChooser} from "./pipelines/pipeline-editor/filter-chooser/filter-chooser.component";
import {PipelinesModule} from "./pipelines/pipelines.module";
import {RouterEffects} from "./router/router.effects";
import {SettingsComponent} from "./settings/settings.component";
import {HeaderBarModule} from "./common/headerbar.module";
import {SidemenuComponent} from "./common/sidemenu/sidemenu.component";
import {LiveEditingModule} from "./filters/filter.module";
import {LoadingEffects} from "./common/loading/loading.effects";
import {ErrorEffects} from "./common/error/error.effects";
import {ParameterList} from "./common/parameter/parameter-list.component";
import {DropzoneComponent} from "./pipelines/pipeline-editor/pipely/dropzone.component";
import {DraggableComponent} from "./pipelines/pipeline-editor/pipely/draggable.component";
import {WorkspaceComponent} from "./pipelines/pipeline-editor/pipely/workspace.component";


const routes: Routes = [
    {path: "", redirectTo: "/dashboard", pathMatch: "full"},
    {path: "dashboard", component: DashboardComponent},
    {path: "agent", loadChildren: () => AgentsModule},
    {path: "pipelines", loadChildren: () => PipelinesModule},
    {path: "filter/:id", loadChildren: () => LiveEditingModule},
    {path: "settings", component: SettingsComponent}
];

export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http);
}
@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        HttpClientModule,
        ReactiveFormsModule,
        RouterModule.forRoot(routes),
        StoreModule.forRoot(reducers, {metaReducers}),
        EffectsModule.forRoot([AppConfigEffects, RouterEffects, LoadingEffects, ErrorEffects]),
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
        HeaderBarModule
    ],
    declarations: [
        AppComponent,
        DashboardComponent,
        FilterChooser,
        SettingsComponent,
        SidemenuComponent
    ],
    providers: [
        AppConfigLoader,
        {provide: APP_INITIALIZER,
            useFactory: (configLoader: AppConfigLoader) => () => configLoader.load(),
            deps: [AppConfigLoader],
            multi: true}
    ],
    entryComponents: [
        FilterChooser,
        SettingsComponent
    ],
    bootstrap: [
        AppComponent
    ]
})
export class AppModule {

}
