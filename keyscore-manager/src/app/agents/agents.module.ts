import {EffectsModule} from "@ngrx/effects";
import {StoreModule} from "@ngrx/store";
import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";
import {AgentsReducer} from "./agents.reducer";
import {AgentsEffects} from "./agents.effects";
import {AgentsComponent} from "./agents.component";

export const routes: Routes = [
    {path: '', component: AgentsComponent},
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
    ],
    providers: []
})
export class AgentsModule { }