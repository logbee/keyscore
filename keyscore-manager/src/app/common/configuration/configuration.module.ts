import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ParameterMap} from "./parameter/parameter-map.component";
import {ParameterList} from "./parameter/parameter-list.component";
import {ParameterComponent} from "./parameter/parameter.component";
import {ParameterControlService} from "./parameter/services/parameter-control.service";
import {TranslateModule} from "@ngx-translate/core";
import {MaterialModule} from "../../material.module";
import {ConfigurationComponent} from "./configuration.component";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MaterialModule
    ],
    declarations: [
        ParameterMap,
        ParameterList,
        ParameterComponent,
        ConfigurationComponent
    ]
    ,
    exports:[
        ConfigurationComponent
    ],
    providers: [ParameterControlService]
})
export class ParameterModule {
}
