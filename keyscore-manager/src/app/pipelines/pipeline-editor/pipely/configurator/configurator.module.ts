import {ConfiguratorComponent} from "./configurator.component";
import {TranslateModule} from "@ngx-translate/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgModule} from "@angular/core";
import {MaterialModule} from "@keyscore-manager-material/src/main/material.module";
import {ParameterModule} from "@keyscore-manager-pipeline-parameters/src/main/parameter.module";

@NgModule({
    imports: [
        CommonModule,
        TranslateModule,
        MaterialModule,
        ParameterModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [
        ConfiguratorComponent
    ],
    entryComponents: [
        ConfiguratorComponent
    ],
    exports: [ConfiguratorComponent]
})

export class ConfiguratorModule {

}