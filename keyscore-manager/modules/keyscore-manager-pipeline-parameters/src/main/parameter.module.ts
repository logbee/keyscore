import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {TranslateModule} from "@ngx-translate/core";
import {DragDropModule} from '@angular/cdk/drag-drop';
import {PropagationStopModule} from "ngx-propagation-stop";
import {ParameterFactoryService} from "./service/parameter-factory.service";
import {TextParameterModule} from "./parameters/text-parameter/text-parameter.module";
import {ExpressionParameterModule} from "./parameters/expression-parameter/expression-parameter.module";
import {StringValidatorService} from "./service/string-validator.service";
import {NumberParameterModule} from "./parameters/number-parameter/number-parameter.module";
import {DecimalParameterModule} from "./parameters/decimal-parameter/decimal-parameter.module";
import {FieldNamePatternParameterModule} from "./parameters/field-name-pattern-parameter/field-name-pattern-parameter.module";
import {FieldNameParameterModule} from "./parameters/field-name-parameter/field-name-parameter.module";
import {BooleanParameterModule} from "./parameters/boolean-parameter/boolean-parameter.module";
import {ParameterFormComponent} from "./parameter-form.component";
import {FieldParameterModule} from "./parameters/field-parameter/field-parameter.module";
import {ListParameterModule} from "./parameters/list-parameter/list-parameter.module";
import {ChoiceParameterModule} from "./parameters/choice-parameter/choice-parameter.module";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";
import {ParameterGroupModule} from "@keyscore-manager-pipeline-parameters/src/main/parameters/parameter-group/parameter-group.module";
import {PasswordParameterModule} from "@keyscore-manager-pipeline-parameters/src/main/parameters/password-parameter/password-parameter.module";
import {DirectiveSequenceParameterModule} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/directive-sequence-parameter.module";


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
        PasswordParameterModule,
        NumberParameterModule,
        DecimalParameterModule,
        FieldNamePatternParameterModule,
        FieldNameParameterModule,
        BooleanParameterModule,
        FieldParameterModule,
        ListParameterModule,
        ChoiceParameterModule,
        DirectiveSequenceParameterModule,
        /*Import ParameterGroupModule as last module because initialisation might depend on the other ParameterModules*/
        ParameterGroupModule
    ],
    declarations: [
        ParameterFormComponent
    ],
    exports: [
        ParameterFormComponent
    ],
    providers: [
        StringValidatorService
    ]
})
export class ParameterModule {

}
