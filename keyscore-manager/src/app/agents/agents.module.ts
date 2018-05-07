import {EffectsModule} from "@ngrx/effects";
import {StoreModule} from "@ngrx/store";
import {RouterModule, Routes} from "@angular/router";
import {NgModule, Pipe} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";
import {AgentsReducer} from "./agents.reducer";
import {AgentsEffects} from "./agents.effects";
import {AgentsComponent} from "./agents.component";
import {AgentsDetails} from "./agents-details/agents-details";
import {TranslateModule} from "@ngx-translate/core";
export const routes: Routes = [
    {path: '', component: AgentsComponent},
    {path: ':id', component: AgentsDetails},
];

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild(routes),
        StoreModule.forFeature('agents', AgentsReducer),
        EffectsModule.forFeature([AgentsEffects]),
        TranslateModule
    ],
    declarations: [
        AgentsComponent,
        AgentsDetails,
    ],
    providers: []
})
export class AgentsModule { }