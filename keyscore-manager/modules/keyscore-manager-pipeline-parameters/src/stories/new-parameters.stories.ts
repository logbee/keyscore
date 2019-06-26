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
import {NumberParameterComponent} from "../main/parameters/number-parameter/number-parameter.component";
import {NumberParameter, NumberParameterDescriptor} from "../main/parameters/number-parameter/number-parameter.model";

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
            descriptor: new ExpressionParameterDescriptor({id: "myexpression"}, "Field Pattern", "", "", [
                new ExpressionParameterChoice("expression.regex", "RegEx", ""),
                new ExpressionParameterChoice("expression.grok", "Grok", ""),
                new ExpressionParameterChoice("expression.glob", "Glob", "")
            ])
            ,
            parameter: new ExpressionParameter({id: "myexpression"}, "Hello World", "regex"),
            emitter: action('Value Change')
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
        descriptor: new TextParameterDescriptor({id: "myTextParameter"},
            "Text Parameter", "My text parameter",
            "Default Value",
            {expression: "*", expressionType: ExpressionType.RegEx, description: "test"}, false)
        ,
        parameter: new TextParameter({id: "myTextParameter"}, "Initial Value"),
        emitter: action('Value Change')
    }
}));

storiesOf('Parameters/NumberParameter', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule
        ],
        providers: []
    })).add("Without Range", () => ({
    component: NumberParameterComponent,
    props: {
        descriptor: new NumberParameterDescriptor({id: "myNumberParameter"},
            "Number Parameter", "My number parameter",
            0,
            null, false)
        ,
        parameter: new NumberParameter({id: "myNumberParameter"}, 0),
        emitter: action('Value Change')
    }
})).add("With Range: 0-10 Step:2", () => ({
    component: NumberParameterComponent,
    props: {
        descriptor: new NumberParameterDescriptor({id: "myNumberParameter"},
            "Number Parameter", "My number parameter",
            0,
            {start: 0, end: 10, step: 2}, false)
        ,
        parameter: new NumberParameter({id: "myNumberParameter"}, 0),
        emitter: action('Value Change')
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
