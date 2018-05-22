import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {FormsModule, ReactiveFormsModule} from '@angular/forms'
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {RouterModule, Routes} from '@angular/router';

import {StoreModule} from '@ngrx/store';

import {AppComponent} from './app.component';
import {DashboardComponent} from "./dashboard/dashboard.component";
import {AppConfigEffects} from "./app.config";
import {FilterChooser} from "./streams/stream-editor/filter-chooser/filter-chooser.component";
import {metaReducers} from "./meta.reducers";
import {reducers} from "./app.reducers";
import {EffectsModule} from "@ngrx/effects";
import {StreamsModule} from "./streams/streams.module";
import {StoreRouterConnectingModule} from '@ngrx/router-store';
import {RouterEffects} from "./router/router.effects";
import {AgentsModule} from "./agents/agents.module";
import {StreamBuilderService} from "./services/streambuilder.service";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {ErrorComponent} from "./failures/error.component";


const routes: Routes = [
    {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
    {path: 'dashboard', component: DashboardComponent},
    {path: 'agent', loadChildren: () => AgentsModule},
    {path: 'streams', loadChildren: () => StreamsModule}
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
        EffectsModule.forRoot([AppConfigEffects, RouterEffects]),
        StoreRouterConnectingModule,
        TranslateModule.forRoot({
            loader:{
                provide:TranslateLoader,
                useFactory:HttpLoaderFactory,
                deps: [HttpClient]
            }
        }),
        //ToDO: Throws DataCloneError
        // StoreDevtoolsModule.instrument({
        //     maxAge:20
        // })
    ],
    declarations: [
        AppComponent,
        DashboardComponent,
        FilterChooser,

    ],
    providers: [
        StreamBuilderService
    ],
    entryComponents: [
        FilterChooser,
    ],
    bootstrap: [
        AppComponent
    ]
})
export class AppModule {

}