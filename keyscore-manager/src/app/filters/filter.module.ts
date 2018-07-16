import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {FilterReducer} from "./filter.reducer";
import {StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";
import {FilterEffects} from "./filters.effects";
import {LoadingModule} from "../common/loading/loading.module";
import {TranslateModule} from "@ngx-translate/core";
import {HeaderBarModule} from "../common/headerbar.module";
import {ErrorModule} from "../common/error/error.module";
import {HealthModule} from "../common/health/health.module";
import {AlertModule} from "../common/alert/alert.module";
import {FilterDescriptionComponent} from "./filter-details/live-editing-modules/filter-description.component";
import {ExampleMessageComponent} from "./filter-details/live-editing-modules/example-message.component";
import {PatternComponent} from "./filter-details/live-editing-modules/pattern.component";
import {FilterResultComponent} from "./filter-details/live-editing-modules/filter-result.component";
import {StatuslightComponent} from "../common/health/statuslight.component";
import {RouterModule, Routes} from "@angular/router";
import {LiveEditingComponent} from "./filter-details/live-editing.component";

export const routes: Routes = [
    {path: "", component: LiveEditingComponent}
];
@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild(routes),
        ReactiveFormsModule,
        StoreModule.forFeature("filter", FilterReducer),
        EffectsModule.forFeature([FilterEffects]),
        TranslateModule,
        HeaderBarModule,
        LoadingModule,
        HealthModule,
        AlertModule,
        ErrorModule
    ],
    declarations: [
        FilterDescriptionComponent,
        ExampleMessageComponent,
        PatternComponent,
        FilterResultComponent,
        StatuslightComponent,
        LiveEditingComponent
    ]
    ,
    providers: [],
})
export class LiveEditingModule {
}
