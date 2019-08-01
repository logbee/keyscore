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
import {ParameterFieldnamepatternComponent} from "./parameter-fieldnamepattern.component";
import {TextParameterModule} from "./parameters/text-parameter/text-parameter.module";
import {ExpressionParameterModule} from "./parameters/expression-parameter/expression-parameter.module";
import {StringValidatorService} from "./service/string-validator.service";
import {NumberParameterModule} from "./parameters/number-parameter/number-parameter.module";
import {DecimalParameterModule} from "./parameters/decimal-parameter/decimal-parameter.module";
import {FieldNamePatternParameterModule} from "./parameters/field-name-pattern-parameter/field-name-pattern-parameter.module";
import {FieldNameParameterModule} from "./parameters/field-name-parameter/field-name-parameter.module";
import {BooleanParameterModule} from "./parameters/boolean-parameter/boolean-parameter.module";
import {MatIconRegistry} from "@angular/material";
import {DomSanitizer} from "@angular/platform-browser";


@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MaterialModule,
        DragDropModule,
        PropagationStopModule,
        ExpressionParameterModule,
        TextParameterModule,
        NumberParameterModule,
        DecimalParameterModule,
        FieldNamePatternParameterModule,
        FieldNameParameterModule,
        BooleanParameterModule
    ],
    declarations: [
        ParameterMap,
        ParameterListComponent,
        ParameterComponent,
        AutocompleteInputComponent,
        ParameterDirectiveComponent,
        ParameterFieldnamepatternComponent

    ],
    exports: [
        ParameterComponent
    ],
    providers: [
        ParameterControlService,
        ParameterFactoryService,
        StringValidatorService
    ]
})
export class ParameterModule {

}
