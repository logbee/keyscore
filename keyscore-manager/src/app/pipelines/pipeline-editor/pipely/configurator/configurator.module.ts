import {ConfiguratorComponent} from "./configurator.component";
import {ParameterModule} from "../../../../common/parameter/parameter.module";
import {MaterialModule} from "../../../../material.module";
import {TranslateModule} from "@ngx-translate/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgModule} from "@angular/core";

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