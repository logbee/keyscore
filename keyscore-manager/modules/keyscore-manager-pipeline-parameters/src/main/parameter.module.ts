import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ParameterMap} from "./parameter-map.component";
import {ParameterListComponent} from "./parameter-list.component";
import {ParameterComponent} from "./parameter.component";
import {TranslateModule} from "@ngx-translate/core";
import {ParameterControlService} from "./service/parameter-control.service";
import {AutocompleteInputComponent} from "./autocomplete-input.component";
import {ParameterDirectiveComponent} from "./parameter-directive.component";
import {DragDropModule} from '@angular/cdk/drag-drop';
import {PropagationStopModule} from "ngx-propagation-stop";
import {ParameterFactoryService} from "./service/parameter-factory.service";
import {MaterialModule} from "keyscore-manager-material";


@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MaterialModule,
        DragDropModule,
        PropagationStopModule
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
        ParameterControlService,
        ParameterFactoryService
    ]
})
export class ParameterModule {
}
