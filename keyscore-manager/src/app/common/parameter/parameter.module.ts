import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ParameterMap} from "../parameter/parameter-map.component";
import {ParameterListComponent} from "./parameter-list.component";
import {ParameterComponent} from "../parameter/parameter.component";
import {TranslateModule} from "@ngx-translate/core";
import {MaterialModule} from "../../material.module";
import {ParameterControlService} from "./service/parameter-control.service";
import {AutocompleteInputComponent} from "./autocomplete-input.component";
import {ParameterDirectiveComponent} from "./parameter-directive.component";
import {DragDropModule} from '@angular/cdk/drag-drop';



@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MaterialModule,
        DragDropModule

    ],
    declarations: [
        ParameterMap,
        ParameterListComponent,
        ParameterComponent,
        AutocompleteInputComponent,
        ParameterDirectiveComponent
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
