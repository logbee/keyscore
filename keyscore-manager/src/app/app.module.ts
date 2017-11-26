import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';
import { RouterModule, Routes }   from '@angular/router';

import { StoreModule } from '@ngrx/store';

import { AppComponent }  from './app.component';
import { DashboardComponent } from "./dashboard/dashboard.component";
import { NodesComponent } from "./nodes/nodes.component";
import { StreamsComponent } from "./streams/streams.component";
import { StreamDetailComponent } from "./streams/stream-detail.component";
import { FiltersComponent } from "./filters/filters.component";
import { FilterDetailComponent } from "./filters/filter-detail.component";
import {streamReducer} from "./streams/stream.reducer";

// Enable configuration described in: https://gist.github.com/fernandohu/122e88c3bcd210bbe41c608c36306db9

const routes: Routes = [
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
    { path: 'dashboard', component: DashboardComponent },
    { path: 'node', component: NodesComponent },
    { path: 'stream', component: StreamsComponent },
    { path: 'stream/:id', component: StreamDetailComponent },
    { path: 'filter', component: FiltersComponent },
    { path: 'filter/details', component: FilterDetailComponent }
];

@NgModule({
  imports:      [
      BrowserModule,
      FormsModule,
      RouterModule.forRoot(routes),
      StoreModule.forRoot({ stream: streamReducer, filterInstances: Object })
  ],
  declarations: [
      AppComponent,
      DashboardComponent,
      NodesComponent,
      StreamsComponent,
      StreamDetailComponent,
      FiltersComponent,
      FilterDetailComponent
  ],
  bootstrap: [ AppComponent ]
})

export class AppModule { }
