import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {RouterModule, Routes} from "@angular/router";
import {EffectsModule} from "@ngrx/effects";
import {TranslateModule} from "@ngx-translate/core";
import {AgentsComponent} from "./agents.component";
import {AgentsEffects} from "./agents.effects";
import {HeaderBarModule} from "../common/headerbar/headerbar.module";
import {LoadingModule} from "../common/loading/loading.module";
import {AppAuthGuard} from "../app.authguard";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";

export const routes: Routes = [
    {path: "", component: AgentsComponent, canActivate: [AppAuthGuard]}
];


@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild(routes),
        EffectsModule.forFeature([AgentsEffects]),
        TranslateModule,
        HeaderBarModule,
        LoadingModule,
        MaterialModule
    ],
    declarations: [
        AgentsComponent
    ],
    providers: [

    ]
})
export class AgentsModule {
}
