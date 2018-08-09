import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterModule, Routes} from "@angular/router";
import {EffectsModule} from "@ngrx/effects";
import {StoreModule} from "@ngrx/store";
import {TranslateModule} from "@ngx-translate/core";
import {ErrorComponent} from "../common/error/error.component";
import {LoadingEffects} from "../common/loading/loading.effects";
import {RefreshTimeComponent} from "../common/loading/refresh.component";
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
import {PipelinesReducer} from "./pipelines.reducer";
import {HeaderBarModule} from "../common/headerbar.module";
import {ErrorEffects} from "../common/error/error.effects";
import {LoadingModule} from "../common/loading/loading.module";
import {HealthModule} from "../common/health/health.module";
import {AlertModule} from "../common/alert/alert.module";
import {ErrorModule} from "../common/error/error.module";
import {PipelyComponent} from "./pipeline-editor/pipely/pipely.component";
import {DraggableComponent} from "./pipeline-editor/pipely/draggable.component";
import {DropzoneComponent} from "./pipeline-editor/pipely/dropzone.component";
import {WorkspaceComponent} from "./pipeline-editor/pipely/workspace.component";

export const routes: Routes = [
    {path: "", component: PipelinesComponent},
    {path: ":id", component: PipelineEditorComponent},
    {path: "error", component: ErrorComponent}
];

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild(routes),
        StoreModule.forFeature("pipelines", PipelinesReducer),
        EffectsModule.forFeature([PipelinesEffects]),
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
        PipelyComponent,
        FilterInformationComponent,
        RefreshTimeComponent,
        ParameterList,
        ParameterMap,
        ParameterComponent,
        DropzoneComponent,
        DraggableComponent,
        WorkspaceComponent
    ],
    providers: [],
    entryComponents: [
        DropzoneComponent,
        DraggableComponent
    ]
})
export class PipelinesModule {
}
