import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";
import {TranslateModule} from "@ngx-translate/core";
import {HeaderBarModule} from "../../common/headerbar/headerbar.module";
import {LoadingModule} from "../../common/loading/loading.module";
import {HealthModule} from "../../common/health/health.module";
import {MaterialModule} from "../../material.module";
import {FilterService} from "../../services/rest-api/filter.service";
import {ResourceViewerComponent} from "./resource-viewer.component";
import {ResourceViewerReducer} from "./resource-viewer.reducer";
import {ResourceViewerEffects} from "./resource-viewer.effects";

export const routes: Routes = [
    {path:"", component: ResourceViewerComponent}
];

@NgModule({
    imports:[
        CommonModule,
        RouterModule.forChild(routes),
        StoreModule.forFeature("resource-viewer", ResourceViewerReducer),
        EffectsModule.forFeature([ResourceViewerEffects]),
        TranslateModule,
        HeaderBarModule,
        LoadingModule,
        HealthModule,
        MaterialModule
    ],
    declarations: [
        ResourceViewerComponent
    ],
    providers: [
        FilterService
    ]
})

export class ResourceViewerModule {
}