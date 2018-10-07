import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterModule, Routes} from "@angular/router";
import {EffectsModule} from "@ngrx/effects";
import {StoreModule} from "@ngrx/store";
import {TranslateModule} from "@ngx-translate/core";
import {ErrorComponent} from "../common/error/error.component";
import {RefreshTimeComponent} from "../common/loading/refresh.component";
import {PipelineEditorComponent} from "./pipeline-editor/pipeline-editor.component";
import {PipelinesComponent} from "./pipelines.component";
import {PipelinesEffects} from "./pipelines.effects";
import {PipelinesReducer} from "./pipelines.reducer";
import {HeaderBarModule} from "../common/headerbar/headerbar.module";
import {LoadingModule} from "../common/loading/loading.module";
import {HealthModule} from "../common/health/health.module";
import {ErrorModule} from "../common/error/error.module";
import {PipelyModule} from "./pipeline-editor/pipely/pipely.module";
import {MaterialModule} from "../material.module";
import {BlueprintService} from "../services/rest-api/BlueprintService";
import {DescriptorResolverService} from "../services/descriptor-resolver.service";
import {PipelyKeyscoreAdapter} from "../services/pipely-keyscore-adapter.service";

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
        ErrorModule,
        PipelyModule,
        MaterialModule
    ],
    declarations: [
        PipelinesComponent,
        PipelineEditorComponent,
        RefreshTimeComponent
    ],
    providers: [
        DescriptorResolverService,
        PipelyKeyscoreAdapter
    ]
})
export class PipelinesModule {
}
