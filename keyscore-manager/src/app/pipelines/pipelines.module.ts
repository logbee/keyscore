import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterModule, Routes} from "@angular/router";
import {EffectsModule} from "@ngrx/effects";
import {StoreModule} from "@ngrx/store";
import {TranslateModule} from "@ngx-translate/core";
import {ErrorComponent} from "../common/error/error.component";
import {PipelineEditorComponent} from "./pipeline-editor/pipeline-editor.component";
import {PipelinesComponent} from "./pipelines.component";
import {HeaderBarModule} from "../common/headerbar/headerbar.module";
import {LoadingModule} from "../common/loading/loading.module";
import {HealthModule} from "../common/health/health.module";
import {ErrorModule} from "../common/error/error.module";
import {PipelyModule} from "./pipeline-editor/pipely/pipely.module";
import {MaterialModule} from "@keyscore-manager-material";
import {PipelyKeyscoreAdapter} from "../services/pipely-keyscore-adapter.service";
import {effects, reducers} from "./index";
import {DatatableModule} from "./datatable/datatable.module";
import {AppAuthGuard} from "../app.authguard";

export const routes: Routes = [
    {path: "", component: PipelinesComponent,canActivate:[AppAuthGuard]},
    {path: ":id", component: PipelineEditorComponent,canActivate:[AppAuthGuard]},
    {path: "error", component: ErrorComponent,canActivate:[AppAuthGuard]}
];

export const routesWithoutAuth: Routes = [
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
        StoreModule.forFeature("state", reducers),
        EffectsModule.forFeature(effects),
        TranslateModule,
        HeaderBarModule,
        LoadingModule,
        HealthModule,
        ErrorModule,
        PipelyModule,
        MaterialModule,
    ],
    declarations: [
        PipelinesComponent,
        PipelineEditorComponent

    ],
    providers: [
        AppAuthGuard,
        PipelyKeyscoreAdapter
    ]
})

export class PipelinesModule {
}
