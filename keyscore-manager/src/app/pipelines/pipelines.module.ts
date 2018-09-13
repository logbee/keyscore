import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterModule, Routes} from "@angular/router";
import {EffectsModule} from "@ngrx/effects";
import {StoreModule} from "@ngrx/store";
import {TranslateModule} from "@ngx-translate/core";
import {ErrorComponent} from "../common/error/error.component";
import {RefreshTimeComponent} from "../common/loading/refresh.component";
import {BlocklyComponent} from "./pipeline-editor/blockly/blockly.component";
import {FilterInformationComponent} from "./pipeline-editor/filter-information.component";
import {PipelineDetailsComponent} from "./pipeline-editor/pipeline-details.component";
import {PipelineEditorComponent} from "./pipeline-editor/pipeline-editor.component";
import {PipelineFilterComponent} from "./pipeline-editor/pipeline-filter.component";
import {PipelinesComponent} from "./pipelines.component";
import {PipelinesEffects} from "./pipelines.effects";
import {PipelinesReducer} from "./pipelines.reducer";
import {HeaderBarModule} from "../common/headerbar.module";
import {LoadingModule} from "../common/loading/loading.module";
import {HealthModule} from "../common/health/health.module";
import {AlertModule} from "../common/alert/alert.module";
import {ErrorModule} from "../common/error/error.module";
import {PipelyComponent} from "./pipeline-editor/pipely/pipely.component";
import {PipelyModule} from "./pipeline-editor/pipely/pipely.module";
import {ParameterModule} from "../common/parameter/parameter.module";
import {MatButtonModule} from '@angular/material/button';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatListModule} from '@angular/material/list';
import {MatDividerModule} from '@angular/material/divider';
import {MatInputModule} from "@angular/material/input";
import {MatTableModule} from "@angular/material/table";
import {MatIconModule} from "@angular/material/icon";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MaterialModule} from "../material.module";

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
        ErrorModule,
        PipelyModule,
        ParameterModule,
        MaterialModule
    ],
    declarations: [
        PipelinesComponent,
        PipelineEditorComponent,
        PipelineDetailsComponent,
        PipelineFilterComponent,
        BlocklyComponent,
        PipelyComponent,
        FilterInformationComponent,
        RefreshTimeComponent
    ],
    providers: []
})
export class PipelinesModule {
}
