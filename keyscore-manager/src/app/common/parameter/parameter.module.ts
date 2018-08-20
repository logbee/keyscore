import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ParameterMap} from "./parameter-map.component";
import {ParameterList} from "./parameter-list.component";
import {ParameterComponent} from "./parameter.component";
import {ParameterControlService} from "./services/parameter-control.service";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule
    ],
    declarations: [
        ParameterMap,
        ParameterList,
        ParameterComponent
    ]
    ,
    exports:[
        ParameterMap,
        ParameterList,
        ParameterComponent
    ],
    providers: [ParameterControlService]
})
export class ParameterModule {
}
