import {CommonModule} from "@angular/common";
import {NgModule, Pipe} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {RouterModule, Routes} from "@angular/router";
import {EffectsModule} from "@ngrx/effects";
import {StoreModule} from "@ngrx/store";
import {TranslateModule} from "@ngx-translate/core";
import {AgentsDetails} from "./agents-details/agents-details";
import {AgentsComponent} from "./agents.component";
import {AgentsEffects} from "./agents.effects";
import {AgentsReducer} from "./agents.reducer";
export const routes: Routes = [
    {path: "", component: AgentsComponent},
    {path: ":id", component: AgentsDetails},
];

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild(routes),
        StoreModule.forFeature("agents", AgentsReducer),
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
