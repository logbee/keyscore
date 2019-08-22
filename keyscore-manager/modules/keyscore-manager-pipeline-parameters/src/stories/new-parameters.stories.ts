import {moduleMetadata, storiesOf} from "@storybook/angular";
import {action} from '@storybook/addon-actions';
import {ExpressionParameterComponent} from "../main/parameters/expression-parameter/expression-parameter.component";
import {MaterialModule} from "keyscore-manager-material";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {
    ExpressionParameter,
    ExpressionParameterChoice,
    ExpressionParameterDescriptor,
} from "../main/parameters/expression-parameter/expression-parameter.model";
import {TextParameterComponent} from "../main/parameters/text-parameter/text-parameter.component";
import {TextParameter, TextParameterDescriptor} from "../main/parameters/text-parameter/text-parameter.model";
import {ExpressionType, FieldNameHint, FieldValueType} from "keyscore-manager-models";
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
import {BooleanParameterModule} from "../main/parameters/boolean-parameter/boolean-parameter.module";
import {FieldNameParameterComponent} from "../main/parameters/field-name-parameter/field-name-parameter.component";
import {
    FieldNameParameter,
    FieldNameParameterDescriptor
} from "../main/parameters/field-name-parameter/field-name-parameter.model";
import {FieldNamePatternParameterComponent} from "../main/parameters/field-name-pattern-parameter/field-name-pattern-parameter.component";
import {
    FieldNamePatternParameter,
    FieldNamePatternParameterDescriptor,
    PatternType,
    PatternTypeChoice
} from "../main/parameters/field-name-pattern-parameter/field-name-pattern-parameter.model";
import {SharedControlsModule} from "../main/shared-controls/shared-controls.module";
import {ValueControlsModule} from "../main/value-controls/value-controls.module";
import {FieldParameterComponent} from "../main/parameters/field-parameter/field-parameter.component";
import {FieldParameter, FieldParameterDescriptor} from "../main/parameters/field-parameter/field-parameter.model";
import {ValueComponentRegistryService} from "../main/value-controls/services/value-component-registry.service";
import {FieldParameterModule} from "../main/parameters/field-parameter/field-parameter.module";
import {TextValue} from "../main/models/value.model";
import {
    TextListParameter,
    TextListParameterDescriptor
} from "../main/parameters/list-parameter/models/text-list-parameter.model";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {ListParameterModule} from "../main/parameters/list-parameter/list-parameter.module";
import {ListParameterComponent} from "../main/parameters/list-parameter/list-parameter.component";
import {ParameterFactoryService} from "../main/service/parameter-factory.service";
import {
    FieldNameListParameter,
    FieldNameListParameterDescriptor
} from "../main/parameters/list-parameter/models/field-name-list-parameter.model";

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
            descriptor: new ExpressionParameterDescriptor({id: "myexpression"},
                "Field Pattern", "", "", true, [
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
})).add("With RegEx Validator, no validator description", () => ({
    component: TextParameterComponent,
    props: {
        descriptor: new TextParameterDescriptor({id: "myTextParameter"},
            "Text Parameter", "My text parameter",
            "Default Value",
            {expression: ".*test", expressionType: ExpressionType.RegEx, description: ""}, true)
        ,
        parameter: new TextParameter({id: "myTextParameter"}, "Initial Value"),
        emitter: action('Value Change')
    }
})).add("With Glob Validator, with validator description", () => ({
    component: TextParameterComponent,
    props: {
        descriptor: new TextParameterDescriptor({id: "myTextParameter"},
            "Path to File", "My text parameter",
            "Default Value",
            {
                expression: "**/*.txt", expressionType: ExpressionType.Glob,
                description: "Path has to point to a '.txt' file in the same directory or a subdirectory"
            }, true)
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
            BrowserAnimationsModule,
            SharedControlsModule
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
            BrowserAnimationsModule,
            SharedControlsModule
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

storiesOf('Parameters/FieldNameParameter', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule,
            SharedControlsModule


        ],
        providers: [StringValidatorService]
    })).add("Present Field Hints", () => ({
    component: FieldNameParameterComponent,
    props: {
        descriptor: new FieldNameParameterDescriptor({id: "myFieldNameParameter"}, "Field Name Parameter",
            "My field name Parameter", "Fieldname", FieldNameHint.PresentField, null, true),
        parameter: new FieldNameParameter({id: "myFieldNameParameter"}, ""),
        autoCompleteDataList: ['message', 'timestamp', 'robo_time', 'logbee_time', 'logbee_id'],
        emitter: action('Value Change')
    }
})).add("Absent Field Hints", () => ({
    component: FieldNameParameterComponent,
    props: {
        descriptor: new FieldNameParameterDescriptor({id: "myFieldNameParameter"}, "Field Name Parameter",
            "My field name Parameter", "Fieldname", FieldNameHint.AbsentField, null, true),
        parameter: new FieldNameParameter({id: "myFieldNameParameter"}, ""),
        autoCompleteDataList: ['message', 'timestamp', 'robo_time', 'logbee_time', 'logbee_id'],
        emitter: action('Value Change')
    }
})).add("Any Field Hints with Validator", () => ({
    component: FieldNameParameterComponent,
    props: {
        descriptor: new FieldNameParameterDescriptor({id: "myFieldNameParameter"}, "Field Name Parameter",
            "My field name Parameter", "Fieldname", FieldNameHint.AnyField,
            {
                expression: ".*_time$",
                description: "Field Name has to end with '_time'", expressionType: ExpressionType.RegEx
            }, true),
        parameter: new FieldNameParameter({id: "myFieldNameParameter"}, ""),
        autoCompleteDataList: ['message', 'timestamp', 'robo_time', 'logbee_time', 'logbee_id'],
        emitter: action('Value Change')
    }
}));

storiesOf('Parameters/FieldNamePatternParameter', module)
    .addDecorator(
        moduleMetadata({
            declarations: [],
            imports: [
                CommonModule,
                MaterialModule,
                BrowserAnimationsModule,
                SharedControlsModule
            ],
            providers: []
        }))
    .add("Hint Present Field", () => ({
        component: FieldNamePatternParameterComponent,
        props: {
            descriptor: new FieldNamePatternParameterDescriptor({id: "myFieldNamePatternParameter"},
                "Field Name Pattern", "", "", FieldNameHint.PresentField, [
                    PatternTypeChoice.fromPatternType(PatternType.None),
                    PatternTypeChoice.fromPatternType(PatternType.RegEx),
                    PatternTypeChoice.fromPatternType(PatternType.Glob)
                ], true)
            ,
            parameter: new FieldNamePatternParameter({id: "myFieldNamePatternParameter"}, "", null),
            autoCompleteDataList: ['message', 'timestamp', 'robo_time', 'logbee_time'],
            emitter: action('Value Change')
        }
    }));

storiesOf('Parameters/FieldParameter', module)
    .addDecorator(
        moduleMetadata({
            declarations: [],
            imports: [
                CommonModule,
                MaterialModule,
                BrowserAnimationsModule,
                ValueControlsModule,
                SharedControlsModule
            ],
            providers: [StringValidatorService, ValueComponentRegistryService]
        }))
    .add("Timestamp with Field Name Hints", () => ({
        component: FieldParameterComponent,
        props: {
            descriptor: new FieldParameterDescriptor({id: "myFieldParameter"},
                "Field", "", "", FieldNameHint.AnyField, null, FieldValueType.Timestamp, true)
            ,
            parameter: new FieldParameter({id: "myFieldParameter"}, null),
            autoCompleteDataList: ['message', 'timestamp', 'robo_time', 'logbee_time'],
            emitter: action('Value Change')
        }
    })).add("Boolean with Field Name Hints", () => ({
    component: FieldParameterComponent,
    props: {
        descriptor: new FieldParameterDescriptor({id: "myFieldParameter"},
            "Field", "", "", FieldNameHint.AnyField, null, FieldValueType.Boolean, true)
        ,
        parameter: new FieldParameter({id: "myFieldParameter"}, null),
        autoCompleteDataList: ['message', 'timestamp', 'robo_time', 'logbee_time'],
        emitter: action('Value Change')
    }
})).add("Text Field with initial value", () => ({
    component: FieldParameterComponent,
    props: {
        descriptor: new FieldParameterDescriptor({id: "myFieldParameter"},
            "Field", "", "message", FieldNameHint.AbsentField, null, FieldValueType.Text, true)
        ,
        parameter: new FieldParameter({id: "myFieldParameter"}, {
            name: "message",
            value: new TextValue("initial value")
        }),
        emitter: action('Value Change')
    }
})).add("Number Field", () => ({
    component: FieldParameterComponent,
    props: {
        descriptor: new FieldParameterDescriptor({id: "myFieldParameter"},
            "Field", "", "message", FieldNameHint.AbsentField, null, FieldValueType.Number, true)
        ,
        parameter: new FieldParameter({id: "myFieldParameter"}, {name: "message", value: null}),
        emitter: action('Value Change')
    }
})).add("Decimal Field", () => ({
    component: FieldParameterComponent,
    props: {
        descriptor: new FieldParameterDescriptor({id: "myFieldParameter"},
            "Field", "", "message", FieldNameHint.AbsentField, null, FieldValueType.Decimal, true)
        ,
        parameter: new FieldParameter({id: "myFieldParameter"}, {name: "message", value: null}),
        emitter: action('Value Change')
    }
})).add("Duration Field", () => ({
    component: FieldParameterComponent,
    props: {
        descriptor: new FieldParameterDescriptor({id: "myFieldParameter"},
            "Field", "", "message", FieldNameHint.AbsentField, null, FieldValueType.Duration, true)
        ,
        parameter: new FieldParameter({id: "myFieldParameter"}, {name: "message", value: null}),
        emitter: action('Value Change')
    }
}));


storiesOf('Parameters/ListParameter', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule,
            ListParameterModule
        ],
        providers: [ParameterComponentFactoryService, ParameterFactoryService]
    })).add("Text", () => {
        return {
            template:`<parameter-list [descriptor]="descriptor" [parameter]="parameter" (parameter)="emitter($event)"></parameter-list>` ,
            props: {
                descriptor: new TextListParameterDescriptor({id: "textList"}, "Text List Parameter",
                    "List of messages to add", new TextParameterDescriptor({id: "myTextParameter"}, "Message",
                        "Represents a single message of a list", "default", null, false), 2, 5),
                parameter: new TextListParameter({id: "textList"}, ['test', 'test1', 'test2']),
                emitter: action('Value Change')
            }
        }
}).add("FieldName", () => {
    return {
        template:`<parameter-list [descriptor]="descriptor" [parameter]="parameter" [autoCompleteDataList]="autoCompleteDataList" (parameter)="emitter($event)"></parameter-list>` ,
        props: {
            descriptor: new FieldNameListParameterDescriptor({id: "fieldNameList"}, "FieldName List Parameter",
                "List of messages to add", new FieldNameParameterDescriptor({id: "myFieldNameParameter"}, "Field Name Parameter",
                    "My field name Parameter", "Fieldname", FieldNameHint.PresentField, null, false), 2, 5),
            parameter: new FieldNameListParameter({id: "myFieldNameParameter"}, ["message","robo_time"]),
            autoCompleteDataList: ['message', 'timestamp', 'robo_time', 'logbee_time', 'logbee_id'],
            emitter: action('Value Change')
        }
    }
});


storiesOf('Parameters/ParameterForm', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule,
            ExpressionParameterModule,
            TextParameterModule,
            NumberParameterModule,
            BooleanParameterModule,
            FieldParameterModule,
            ListParameterModule
        ],
        providers: [
            ParameterComponentFactoryService,
            StringValidatorService
        ]
    })).add("default", () => ({
    component: ParameterFormComponent,
    props: {
        parameters: {
            refs: ['expressionParameter', 'textParameter', 'numberParameter', 'booleanParameter', 'fieldParameter', 'textListParameter'],
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
                    new TextParameterDescriptor({id: 'textParameter'}, "Text Parameter",
                        "", "", null, true)],
                'numberParameter': [new NumberParameter({id: "numberParameter"}, 0),
                    new NumberParameterDescriptor({id: "numberParameter"},
                        "Number Parameter", "My number parameter",
                        0,
                        {start: 0, end: 10, step: 2}, false)],
                'booleanParameter': [new BooleanParameter({id: "booleanParameter"}, false),
                    new BooleanParameterDescriptor({id: "booleanParameter"}, "Boolean Parameter",
                        "My boolean Parameter", false, true)],
                'fieldParameter': [new FieldParameter({id: "fieldParameter"}, null),
                    new FieldParameterDescriptor({id: "fieldParameter"},
                        "Field", "", "", FieldNameHint.AnyField, null,
                        FieldValueType.Timestamp, true)],
                'textListParameter': [
                    new TextListParameter({id: 'textListParameter'}, ['init1', 'init2']),
                    new TextListParameterDescriptor({id: 'textListParameter'}, 'Text List',
                        'A text list', new TextParameterDescriptor({id: 'textParameterInList'},
                            'message', '', 'default', null, false), 2, 6)
                ]
            },
            autoCompleteDataList: ['message', 'timestamp', 'robo_time', 'logbee_time']
        },
        onValueChange: action('Value changed')

    }
}));
