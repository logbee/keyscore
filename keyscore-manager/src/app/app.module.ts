import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {FormsModule} from '@angular/forms'
import {HttpClientModule} from "@angular/common/http";
import {RouterModule, Routes} from '@angular/router';

import {StoreModule} from '@ngrx/store';

import {AppComponent} from './app.component';
import {DashboardComponent} from "./dashboard/dashboard.component";
import {NodesComponent} from "./agents/agents.component";
import {StreamsComponent} from "./streams/streams.component";
import {StreamEditorComponent} from "./streams/stream-editor/stream-editor.component";
import {FiltersComponent} from "./filters/filters.component";
import {FilterDetailComponent} from "./filters/filter-detail.component";
import {AppConfigEffects} from "./app.config";
import {FilterChooser} from "./streams/stream-editor/filter-chooser.component";
import {metaReducers} from "./meta.reducers";
import {reducers} from "./reducers";
import {EffectsModule} from "@ngrx/effects";
import {FilterService} from "./services/filter.service";
import {streamsReducers} from "./streams/streams.reducer";

const routes: Routes = [
    {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
    {path: 'dashboard', component: DashboardComponent},
    {path: 'node', component: NodesComponent},
    {path: 'stream', component: StreamsComponent},
    {path: 'stream/:id', component: StreamEditorComponent},
    {path: 'filter', component: FiltersComponent},
    {path: 'filter/details', component: FilterDetailComponent}
];

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        HttpClientModule,
        RouterModule.forRoot(routes),
        StoreModule.forRoot(reducers, {metaReducers}),
        StoreModule.forFeature('streams', streamsReducers, {metaReducers}),
        EffectsModule.forRoot([AppConfigEffects, FilterService])
    ],
    declarations: [
        AppComponent,
        DashboardComponent,
        NodesComponent,
        StreamsComponent,
        StreamEditorComponent,
        FiltersComponent,
        FilterDetailComponent,
        FilterChooser
    ],
    providers: [

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