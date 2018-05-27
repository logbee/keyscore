import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {PipelinesComponent} from "./pipelines.component";
import {CommonModule} from "@angular/common";
import {PipelineEditorComponent} from "./pipeline-editor/pipeline-editor.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {PipelinesReducer} from "./pipelines.reducer";
import {ActionReducerMap, StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";
import {PipelinesEffects} from "./pipelines.effects";
import {PipelineDetailsComponent} from "./pipeline-editor/pipeline-details.component";
import {PipelineFilterComponent} from "./pipeline-editor/pipeline-filter.component";
import {ParameterList} from "./pipeline-editor/filter-editor/parameter-list.component";
import {ParameterComponent} from "./pipeline-editor/filter-editor/parameter.component";
import {ParameterMap} from "./pipeline-editor/filter-editor/parameter-map.component";
import {BlocklyComponent} from "./pipeline-editor/blockly/blockly.component";
import {TranslateModule} from "@ngx-translate/core";
import {FilterEffects} from "./filters/filters.effects";
import {FilterReducer} from "./filters/filter.reducer";
import {FiltersComponent} from "./filters/filters.component";
import {LiveEditingComponent} from "./filters/filter-details/live-editing.component";
import {PipelinesModuleState} from "./pipelines.model";
import {ErrorComponent} from "../failures/error.component";


export const routes: Routes = [
    {path: 'pipeline', component: PipelinesComponent},
    {path: 'pipeline/:id', component: PipelineEditorComponent},
    {path: 'filter', component: FiltersComponent},
    {path: 'filter/:id', component: LiveEditingComponent}
];

export const reducers: ActionReducerMap<PipelinesModuleState> = {
    pipelines: PipelinesReducer,
    filter: FilterReducer
};

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild(routes),
        StoreModule.forFeature('pipelines', reducers),
        EffectsModule.forFeature([PipelinesEffects, FilterEffects]),
        TranslateModule
    ],
    declarations: [
        PipelinesComponent,
        PipelineEditorComponent,
        PipelineDetailsComponent,
        PipelineFilterComponent,
        BlocklyComponent,
        ParameterList,
        ParameterMap,
        ParameterComponent,
        LiveEditingComponent,
        FiltersComponent,
        ErrorComponent
    ],
    providers: []
})
export class PipelinesModule {
}