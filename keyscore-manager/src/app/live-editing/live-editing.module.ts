import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {LiveEditingReducer} from "./live-editing.reducer";
import {StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";
import {LoadingModule} from "../common/loading/loading.module";
import {TranslateModule} from "@ngx-translate/core";
import {HeaderBarModule} from "../common/headerbar/headerbar.module";
import {ErrorModule} from "../common/error/error.module";
import {HealthModule} from "../common/health/health.module";
import {RouterModule, Routes} from "@angular/router";
import {LiveEditingComponent} from "./live-editing.component";
import {MaterialModule} from "../material.module";
import {FiltersEffects} from "./live-editing-effects";
import {DescriptorResolverService} from "../services/descriptor-resolver.service";
import {DatasetTable} from "./components/dataset-table";
import {ValueType} from "./components/value-type";
import {NavigationControlComponent} from "./components/navigation-control.component";
import {ConfiguratorComponent} from "../pipelines/pipeline-editor/pipely/configurator.component";
import {ParameterModule} from "../common/parameter/parameter.module";

export const routes: Routes = [
    {path: "", component: LiveEditingComponent}
];
@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild(routes),
        ReactiveFormsModule,
        StoreModule.forFeature("filter", LiveEditingReducer),
        EffectsModule.forFeature([FiltersEffects]),
        TranslateModule,
        HeaderBarModule,
        LoadingModule,
        HealthModule,
        ErrorModule,
        MaterialModule,
        ParameterModule,
    ],
    declarations: [
        LiveEditingComponent,
        DatasetTable,
        ValueType,
        NavigationControlComponent,
        ConfiguratorComponent
    ]
    ,
    providers: [
        DescriptorResolverService
    ],
})
export class LiveEditingModule {
}
