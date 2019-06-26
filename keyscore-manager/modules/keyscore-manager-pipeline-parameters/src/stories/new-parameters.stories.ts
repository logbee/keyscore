import {moduleMetadata, storiesOf} from "@storybook/angular";
import {action} from '@storybook/addon-actions';
import {ExpressionParameterComponent} from "../main/parameters/expression-parameter/expression-parameter.component";
import {MaterialModule} from "keyscore-manager-material";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {of} from "rxjs";
import {
    ExpressionParameterChoice,
    ExpressionParameter,
    ExpressionParameterDescriptor,
} from "../main/parameters/expression-parameter/expression-parameter.model";
import {TextParameterComponent} from "../main/parameters/text-parameter/text-parameter.component";
import {TextParameter, TextParameterDescriptor} from "../main/parameters/text-parameter/text-parameter.model";
import {ExpressionType} from "keyscore-manager-models";
import {ExpressionParameterModule} from "../main/parameters/expression-parameter/expression-parameter.module";
import {TextParameterModule} from "../main/parameters/text-parameter/text-parameter.module";
import {ParameterComponentFactoryService} from "../main/service/parameter-component-factory.service";
import {ParameterFormComponent} from "../main/parameter-form.component";

storiesOf('Parameters/ExpressionParameter', module)
    .addDecorator(
        moduleMetadata({
            declarations: [],
            imports: [
                CommonModule,
                MaterialModule,
                BrowserAnimationsModule
                // ExpressionParameterModule
            ],
            providers: []
        }))
    .add("default", () => ({
        component: ExpressionParameterComponent,
        props: {
            descriptor$: of(new ExpressionParameterDescriptor({id: "myexpression"}, "Field Pattern", "", "", [
                    new ExpressionParameterChoice("expression.regex", "RegEx", ""),
                    new ExpressionParameterChoice("expression.grok", "Grok", ""),
                    new ExpressionParameterChoice("expression.glob", "Glob", "")
                ]),
            ),
            parameter$: of(new ExpressionParameter({id: "myexpression"}, "Hello World", "regex"))
        }
    }));

storiesOf('Parameters/TextParameter', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule
        ],
        providers: []
    })).add("default", () => ({
    component: TextParameterComponent,
    props: {
        descriptor$: of(new TextParameterDescriptor({id: "myTextParameter"},
            "Text Parameter", "My text parameter",
            "Default Value",
            {expression: "*", expressionType: ExpressionType.RegEx, description: "test"}, false)
        ),
        parameter$: of(new TextParameter({id: "myTextParameter"}, "Initial Value"))
    }
}));

storiesOf('Parameters/ParameterForm', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule,
            ExpressionParameterModule,
            TextParameterModule
        ],
        providers: [
            ParameterComponentFactoryService
        ]
    })).add("default", () => ({
    component: ParameterFormComponent,
    props: {
        parameters: {
            refs: ['expressionParameter', 'textParameter'],
            parameters: {
                'expressionParameter': [new ExpressionParameter({id: 'textParameter'}, 'initialValue', 'regex'),
                    new ExpressionParameterDescriptor({id: "expressionParameter"}, "Field Pattern",
                        "", "", [
                            new ExpressionParameterChoice("expression.regex", "RegEx", ""),
                            new ExpressionParameterChoice("expression.grok", "Grok", ""),
                            new ExpressionParameterChoice("expression.glob", "Glob", "")
                        ])],
                'textParameter': [
                    new TextParameter({id: 'textParameter'}, "initialValue"),
                    new TextParameterDescriptor({id: 'textParameter'}, "Text Parameter", "", "", null, true)]
            }
        },
        onValueChange: action('Value changed')

    }
}));
