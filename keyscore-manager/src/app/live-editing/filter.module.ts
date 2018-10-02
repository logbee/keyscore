import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {FilterReducer} from "./filter.reducer";
import {StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";
import {LoadingModule} from "../common/loading/loading.module";
import {TranslateModule} from "@ngx-translate/core";
import {HeaderBarModule} from "../common/headerbar/headerbar.module";
import {ErrorModule} from "../common/error/error.module";
import {HealthModule} from "../common/health/health.module";
import {AlertModule} from "../common/alert/alert.module";
import {FilterDescriptionComponent} from "./components/filter-description.component";
import {ExampleMessageComponent} from "./components/example-message.component";
import {FilterConfigurationComponent} from "./components/filter-configuration.component";
import {FilterResultComponent} from "./components/filter-result.component";
import {RouterModule, Routes} from "@angular/router";
import {LiveEditingComponent} from "./live-editing.component";
import {DatasetVisualizer} from "./components/datasetVisualizer";
import {MaterialModule} from "../material.module";
import {FilterService} from "../services/rest-api/filter.service";
import {FiltersEffects} from "./filter.effects";

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
        EffectsModule.forFeature([FiltersEffects]),
        TranslateModule,
        HeaderBarModule,
        LoadingModule,
        HealthModule,
        AlertModule,
        ErrorModule,
        MaterialModule



    ],
    declarations: [
        FilterDescriptionComponent,
        ExampleMessageComponent,
        FilterConfigurationComponent,
        FilterResultComponent,
        LiveEditingComponent,
        DatasetVisualizer
    ]
    ,
    providers: [
        FilterService
    ],
})
export class LiveEditingModule {
}
