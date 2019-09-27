import {NgModule} from "@angular/core";
import {ParameterGroupComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/parameter-group/parameter-group.component";
import {CommonModule} from "@angular/common";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
    imports:[
        CommonModule,
        TranslateModule
    ],
    declarations:[
        ParameterGroupComponent
    ],
    exports:[
        ParameterGroupComponent
    ],
    entryComponents:[
        ParameterGroupComponent
    ]
})
export class ParameterGroupModule{

}