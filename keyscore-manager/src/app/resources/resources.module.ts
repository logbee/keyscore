import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";
import {TranslateModule} from "@ngx-translate/core";
import {ResourcesComponent} from "./resources.component";
import {ResourcesReducer} from "./resources.reducer";
import {ResourcesEffects} from "./resources.effects";
import {HeaderBarModule} from "../common/headerbar/headerbar.module";
import {LoadingModule} from "../common/loading/loading.module";
import {HealthModule} from "../common/health/health.module";
import {MaterialModule} from "../material.module";
import {FilterService} from "../services/rest-api/filter.service";
import {ResourceType} from "./components/resource-type";
import {ReactiveFormsModule} from "@angular/forms";
import {ResourcesHealthComponent} from "../common/health/resources-health.component";
import {NgxJsonViewerModule} from 'ngx-json-viewer'
import {JsonVisualizer} from "./components/json-visualizer";
import {DescriptorResolverService} from "../services/descriptor-resolver.service";
import {RestCallService} from "../services/rest-api/rest-call.service";

export const routes: Routes = [
    {path:"", component: ResourcesComponent}
];

@NgModule({
    imports:[
        CommonModule,
        RouterModule.forChild(routes),
        StoreModule.forFeature("resource-viewer", ResourcesReducer),
        EffectsModule.forFeature([ResourcesEffects]),
        TranslateModule,
        HeaderBarModule,
        LoadingModule,
        HealthModule,
        MaterialModule,
        ReactiveFormsModule,
        NgxJsonViewerModule
    ],
    declarations: [
        ResourcesComponent,
        ResourceType,
        ResourcesHealthComponent,
        JsonVisualizer
    ],
    providers: [
        FilterService,
        DescriptorResolverService,
        RestCallService

    ]
})

export class ResourcesModule {

}