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
import {FilterConfigurationComponent} from "./filter-details/live-editing-modules/filter-configuration.component";
import {FilterResultComponent} from "./filter-details/live-editing-modules/filter-result.component";
import {StatuslightComponent} from "../common/health/statuslight.component";
import {RouterModule, Routes} from "@angular/router";
import {LiveEditingComponent} from "./filter-details/live-editing.component";
import {DatasetVisualizer} from "./filter-details/live-editing-modules/datasetVisualizer";
import {ParameterModule} from "../common/parameter/parameter.module";
import {MatCardModule} from '@angular/material/card';
import {MatGridListModule} from "@angular/material/grid-list";
import {MatListModule} from "@angular/material/list";
import {MatDividerModule} from "@angular/material/divider";
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatTableModule} from '@angular/material/table';
import {MatInputModule} from "@angular/material/input";
import {MatFormFieldModule}  from '@angular/material/form-field';
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MaterialModule} from "../material.module";

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
        ErrorModule,
        ParameterModule,
        MaterialModule



    ],
    declarations: [
        FilterDescriptionComponent,
        ExampleMessageComponent,
        FilterConfigurationComponent,
        FilterResultComponent,
        StatuslightComponent,
        LiveEditingComponent,
        DatasetVisualizer
    ]
    ,
    providers: [],
})
export class LiveEditingModule {
}
