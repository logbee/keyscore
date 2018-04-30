import {EffectsModule} from "@ngrx/effects";
import {StoreModule} from "@ngrx/store";
import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";
import {AgentsReducer} from "./agents.reducer";
import {AgentsEffects} from "./agents.effects";
import {AgentsComponent} from "./agents.component";
import {AgentsDetails} from "./agentDetailedView/agents-details";

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
        EffectsModule.forFeature([AgentsEffects])
    ],
    declarations: [
        AgentsComponent,
        AgentsDetails
    ],
    providers: []
})
export class AgentsModule { }