import {APP_INITIALIZER, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {FormsModule} from '@angular/forms'
import {HttpClientModule} from "@angular/common/http";
import {RouterModule, Routes} from '@angular/router';

import {StoreModule} from '@ngrx/store';

import {AppComponent} from './app.component';
import {DashboardComponent} from "./dashboard/dashboard.component";
import {NodesComponent} from "./agents/agents.component";
import {StreamsComponent} from "./streams/streams.component";
import {StreamDetailComponent} from "./streams/stream-detail.component";
import {FiltersComponent} from "./filters/filters.component";
import {FilterDetailComponent} from "./filters/filter-detail.component";
import {streamReducer} from "./streams/stream.reducer";
import {AppConfig} from "./app.config";
import {AddFilterDialog} from "./streams/add-filter-dialog.component";
import {ModalService} from "./services/modal.service";

const routes: Routes = [
    {path: '', redirectTo: '/dashboard', pathMatch: 'full'},
    {path: 'dashboard', component: DashboardComponent},
    {path: 'node', component: NodesComponent},
    {path: 'stream', component: StreamsComponent},
    {path: 'stream/:id', component: StreamDetailComponent},
    {path: 'filter', component: FiltersComponent},
    {path: 'filter/details', component: FilterDetailComponent}
];

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        HttpClientModule,
        RouterModule.forRoot(routes),
        StoreModule.forRoot({stream: streamReducer, filterInstances: Object})
    ],
    declarations: [
        AppComponent,
        DashboardComponent,
        NodesComponent,
        StreamsComponent,
        StreamDetailComponent,
        FiltersComponent,
        FilterDetailComponent,
        AddFilterDialog
    ],
    providers: [
        AppConfig,
        {
            provide: APP_INITIALIZER,
            useFactory: (config: AppConfig) => () => config.load(),
            deps: [AppConfig],
            multi: true
        },
        ModalService
    ],
    entryComponents: [
        AddFilterDialog
    ],
    bootstrap: [AppComponent]
})

export class AppModule {

}