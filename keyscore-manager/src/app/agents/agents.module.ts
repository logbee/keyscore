import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {RouterModule, Routes} from "@angular/router";
import {EffectsModule} from "@ngrx/effects";
import {StoreModule} from "@ngrx/store";
import {TranslateModule} from "@ngx-translate/core";
import {AgentsDetails} from "./agents-details/agents-details";
import {AgentsComponent} from "./agents.component";
import {AgentsEffects} from "./agents.effects";
import {AgentsReducer} from "./agents.reducer";
import {HeaderBarModule} from "../common/headerbar/headerbar.module";
import {LoadingModule} from "../common/loading/loading.module";
import {MaterialModule} from "../material.module";

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
        TranslateModule,
        HeaderBarModule,
        LoadingModule,
        MaterialModule
    ],
    declarations: [
        AgentsComponent,
        AgentsDetails,
    ],
    providers: []
})
export class AgentsModule { }
