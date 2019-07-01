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
import {StringValidatorService} from "../main/service/string-validator.service";
import {
    DecimalParameter,
    DecimalParameterDescriptor
} from "../main/parameters/decimal-parameter/decimal-parameter.model";
import {DecimalParameterComponent} from "../main/parameters/decimal-parameter/decimal-parameter.component";
import {NumberParameterModule} from "../main/parameters/number-parameter/number-parameter.module";
import {BooleanParameterComponent} from "../main/parameters/boolean-parameter/boolean-parameter.component";
import {
    BooleanParameter,
    BooleanParameterDescriptor
} from "../main/parameters/boolean-parameter/boolean-parameter.model";

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
            descriptor: new ExpressionParameterDescriptor({id: "myexpression"}, "Field Pattern", "", "", true, [
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
        providers: [StringValidatorService]
    })).add("default", () => ({
    component: TextParameterComponent,
    props: {
        descriptor: new TextParameterDescriptor({id: "myTextParameter"},
            "Text Parameter", "My text parameter",
            "Default Value",
            null, false)
        ,
        parameter: new TextParameter({id: "myTextParameter"}, "Initial Value"),
        emitter: action('Value Change')
    }
})).add("With RegEx Validator", () => ({
    component: TextParameterComponent,
    props: {
        descriptor: new TextParameterDescriptor({id: "myTextParameter"},
            "Text Parameter", "My text parameter",
            "Default Value",
            {expression: ".*test", expressionType: ExpressionType.RegEx, description: "test"}, true)
        ,
        parameter: new TextParameter({id: "myTextParameter"}, "Initial Value"),
        emitter: action('Value Change')
    }
})).add("With Glob Validator", () => ({
    component: TextParameterComponent,
    props: {
        descriptor: new TextParameterDescriptor({id: "myTextParameter"},
            "Text Parameter", "My text parameter",
            "Default Value",
            {expression: "**/*.txt", expressionType: ExpressionType.Glob, description: "test"}, true)
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

storiesOf('Parameters/DecimalParameter', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule
        ],
        providers: []
    })).add("Without Range", () => ({
    component: DecimalParameterComponent,
    props: {
        descriptor: new DecimalParameterDescriptor({id: "myDecimalParameter"},
            "Decimal Parameter", "My decimal parameter",
            0.00,
            null, 2, true)
        ,
        parameter: new DecimalParameter({id: "myDecimalParameter"}, 0.00),
        emitter: action('Value Change')
    }
})).add("With Range: 0-1 Step:0.02", () => ({
    component: DecimalParameterComponent,
    props: {
        descriptor: new DecimalParameterDescriptor({id: "myDecimalParameter"},
            "Decimal Parameter", "My decimal parameter",
            0.00,
            {start: 0.00, end: 1.00, step: 0.02}, 2, true)
        ,
        parameter: new DecimalParameter({id: "myDecimalParameter"}, 0),
        emitter: action('Value Change')
    }
}));

storiesOf('Parameters/BooleanParameter', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule
        ],
        providers: []
    })).add("default", () => ({
    component: BooleanParameterComponent,
    props: {
        descriptor: new BooleanParameterDescriptor({id: "myBooleanParameter"}, "Boolean Parameter",
            "My boolean Parameter", false, true),
        parameter: new BooleanParameter({id: "myBooleanParameter"}, false),
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
            TextParameterModule,
            NumberParameterModule
        ],
        providers: [
            ParameterComponentFactoryService,
            StringValidatorService
        ]
    })).add("default", () => ({
    component: ParameterFormComponent,
    props: {
        parameters: {
            refs: ['expressionParameter', 'textParameter', 'numberParameter'],
            parameters: {
                'expressionParameter': [new ExpressionParameter({id: 'textParameter'}, 'initialValue', 'regex'),
                    new ExpressionParameterDescriptor({id: "expressionParameter"}, "Field Pattern",
                        "", "", true, [
                            new ExpressionParameterChoice("expression.regex", "RegEx", ""),
                            new ExpressionParameterChoice("expression.grok", "Grok", ""),
                            new ExpressionParameterChoice("expression.glob", "Glob", "")
                        ])],
                'textParameter': [
                    new TextParameter({id: 'textParameter'}, "initialValue"),
                    new TextParameterDescriptor({id: 'textParameter'}, "Text Parameter", "", "", null, true)],
                'numberParameter': [new NumberParameter({id: "myNumberParameter"}, 0),
                    new NumberParameterDescriptor({id: "myNumberParameter"},
                        "Number Parameter", "My number parameter",
                        0,
                        {start: 0, end: 10, step: 2}, false)]
            }
        },
        onValueChange: action('Value changed')

    }
}));
