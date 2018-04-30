import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {FormsModule, ReactiveFormsModule} from '@angular/forms'
import {HttpClientModule} from "@angular/common/http";
import {RouterModule, Routes} from '@angular/router';

import {StoreModule} from '@ngrx/store';

import {AppComponent} from './app.component';
import {DashboardComponent} from "./dashboard/dashboard.component";
import {FiltersComponent} from "./filters/filters.component";
import {FilterDetailComponent} from "./filters/filter-detail.component";
import {AppConfigEffects} from "./app.config";
import {FilterChooser} from "./streams/stream-editor/filter-chooser/filter-chooser.component";
import {metaReducers} from "./meta.reducers";
import {reducers} from "./app.reducers";
import {EffectsModule} from "@ngrx/effects";
import {StreamsModule} from "./streams/streams.module";
import {StoreRouterConnectingModule} from '@ngrx/router-store';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {RouterEffects} from "./router/router.effects";
import {AgentsModule} from "./agents/agents.module";
import {ParameterList} from "./streams/stream-editor/filter-editor/parameter-list/parameter-list.component";
import {ParameterComponent} from "./streams/stream-editor/filter-editor/parameter.component";
import {StreamBuilderService} from "./services/streambuilder.service";
import {BlocklyComponent} from "./streams/stream-editor/blockly/blockly.component";

const routes: Routes = [
    {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
    {path: 'dashboard', component: DashboardComponent},
    {path: 'agent', loadChildren: () => AgentsModule},
    {path: 'stream', loadChildren: () => StreamsModule},
    {path: 'filter', component: FiltersComponent},
    {path: 'filter/details', component: FilterDetailComponent}
];

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
        //ToDO: Throws DataCloneError
        /*StoreDevtoolsModule.instrument({
            maxAge:20
        })*/
    ],
    declarations: [
        AppComponent,
        DashboardComponent,
        FiltersComponent,
        FilterDetailComponent,
        FilterChooser
    ],
    providers: [
        StreamBuilderService
    ],
    entryComponents: [
        FilterChooser
    ],
    bootstrap: [
        AppComponent
    ]
})
export class AppModule {

}