import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ParameterMap} from "../parameter/parameter-map.component";
import {FieldParameterList} from "./field-parameter-list.component";
import {ParameterComponent} from "../parameter/parameter.component";
import {TranslateModule} from "@ngx-translate/core";
import {MaterialModule} from "../../material.module";
import {ParameterControlService} from "./service/parameter-control.service";
import {TextParameterList} from "./text-parameter-list.component";
import {FieldNameInputComponent} from "./field-name-input";


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
        FieldParameterList,
        TextParameterList,
        ParameterComponent,
        FieldNameInputComponent
    ],
    exports: [
        ParameterComponent
    ],
    providers: [
        ParameterControlService
    ]
})
export class ParameterModule {
}
