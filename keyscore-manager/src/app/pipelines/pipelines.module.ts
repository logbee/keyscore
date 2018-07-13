import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterModule, Routes} from "@angular/router";
import {EffectsModule} from "@ngrx/effects";
import {ActionReducerMap, StoreModule} from "@ngrx/store";
import {TranslateModule} from "@ngx-translate/core";
import {ErrorComponent} from "../common/error/error.component";
import {LoadingFullViewComponent} from "../common/loading/loading-full-view.component";
import {LoadingComponent} from "../common/loading/loading.component";
import {LoadingEffects} from "../common/loading/loading.effects";
import {RefreshTimeComponent} from "../common/loading/refresh.component";
import {ExampleMessageComponent} from "./filters/filter-details/live-editing-modules/example-message.component";
import {FilterDescriptionComponent} from "./filters/filter-details/live-editing-modules/filter-description.component";
import {FilterResultComponent} from "./filters/filter-details/live-editing-modules/filter-result.component";
import {PatternComponent} from "./filters/filter-details/live-editing-modules/pattern.component";
import {LiveEditingComponent} from "./filters/filter-details/live-editing.component";
import {FilterReducer} from "./filters/filter.reducer";
import {FiltersComponent} from "./filters/filters.component";
import {FilterEffects} from "./filters/filters.effects";
import {BlocklyComponent} from "./pipeline-editor/blockly/blockly.component";
import {ParameterList} from "./pipeline-editor/filter-editor/parameter-list.component";
import {ParameterMap} from "./pipeline-editor/filter-editor/parameter-map.component";
import {ParameterComponent} from "./pipeline-editor/filter-editor/parameter.component";
import {FilterInformationComponent} from "./pipeline-editor/filter-information.component";
import {PipelineDetailsComponent} from "./pipeline-editor/pipeline-details.component";
import {PipelineEditorComponent} from "./pipeline-editor/pipeline-editor.component";
import {PipelineFilterComponent} from "./pipeline-editor/pipeline-filter.component";
import {PipelinesComponent} from "./pipelines.component";
import {PipelinesEffects} from "./pipelines.effects";
import {PipelinesModuleState} from "./pipelines.model";
import {PipelinesReducer} from "./pipelines.reducer";
import {HeaderBarModule} from "../common/headerbar.module";
import {HealthComponent} from "../common/health/health.component";
import {ErrorEffects} from "../common/error/error.effects";
import {AlertComponent} from "../common/alert/alert.component";
import {AppModule} from "../app.module";
import {StatuslightComponent} from "../common/health/statuslight.component";
import {LoadingModule} from "../common/loading/loading.module";
import {HealthModule} from "../common/health/health.module";
import {AlertModule} from "../common/alert/alert.module";
import {ErrorModule} from "../common/error/error.module";

export const routes: Routes = [
    {path: "pipeline", component: PipelinesComponent},
    {path: "pipeline/:id", component: PipelineEditorComponent},
    {path: "filter", component: FiltersComponent},
    {path: "filter/:id", component: LiveEditingComponent},
    {path: "error", component: ErrorComponent}
];

export const reducers: ActionReducerMap<PipelinesModuleState> = {
    pipelines: PipelinesReducer,
    filter: FilterReducer,
};

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild(routes),
        StoreModule.forFeature("pipelines", reducers),
        EffectsModule.forFeature([PipelinesEffects, FilterEffects, LoadingEffects, ErrorEffects]),
        TranslateModule,
        HeaderBarModule,
        LoadingModule,
        HealthModule,
        AlertModule,
        ErrorModule
    ],
    declarations: [
        PipelinesComponent,
        PipelineEditorComponent,
        PipelineDetailsComponent,
        PipelineFilterComponent,
        BlocklyComponent,
        FilterInformationComponent,
        StatuslightComponent,
        RefreshTimeComponent,
        ParameterList,
        ParameterMap,
        ParameterComponent,
        LiveEditingComponent,
        FiltersComponent,
        FilterDescriptionComponent,
        ExampleMessageComponent,
        PatternComponent,
        FilterResultComponent,
    ],
    providers: [],
})
export class PipelinesModule {
}
