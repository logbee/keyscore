import {moduleMetadata, storiesOf} from "@storybook/angular";
import {action} from '@storybook/addon-actions';
import {ExpressionParameterComponent} from "../main/parameters/expression-parameter/expression-parameter.component";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {TextParameterComponent} from "../main/parameters/text-parameter/text-parameter.component";
import {ExpressionParameterModule} from "../main/parameters/expression-parameter/expression-parameter.module";
import {TextParameterModule} from "../main/parameters/text-parameter/text-parameter.module";
import {ParameterComponentFactoryService} from "../main/service/parameter-component-factory.service";
import {ParameterFormComponent} from "../main/parameter-form.component";
import {NumberParameterComponent} from "../main/parameters/number-parameter/number-parameter.component";
import {StringValidatorService} from "../main/service/string-validator.service";

import {DecimalParameterComponent} from "../main/parameters/decimal-parameter/decimal-parameter.component";
import {NumberParameterModule} from "../main/parameters/number-parameter/number-parameter.module";
import {BooleanParameterComponent} from "../main/parameters/boolean-parameter/boolean-parameter.component";

import {BooleanParameterModule} from "../main/parameters/boolean-parameter/boolean-parameter.module";
import {FieldNameParameterComponent} from "../main/parameters/field-name-parameter/field-name-parameter.component";

import {FieldNamePatternParameterComponent} from "../main/parameters/field-name-pattern-parameter/field-name-pattern-parameter.component";
import {SharedControlsModule} from "../main/shared-controls/shared-controls.module";
import {ValueControlsModule} from "../main/value-controls/value-controls.module";
import {FieldParameterComponent} from "../main/parameters/field-parameter/field-parameter.component";

import {ValueComponentRegistryService} from "../main/value-controls/services/value-component-registry.service";
import {FieldParameterModule} from "../main/parameters/field-parameter/field-parameter.module";

import {ListParameterModule} from "../main/parameters/list-parameter/list-parameter.module";

import {ChoiceParameterComponent} from "../main/parameters/choice-parameter/choice-parameter.component";
import {ReactiveFormsModule} from "@angular/forms";
import {
    ExpressionParameterDescriptor,
    ExpressionParameterChoice,
    ExpressionParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/expression-parameter.model";
import {
    TextParameterDescriptor,
    TextParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/text-parameter.model";
import {
    PasswordParameterDescriptor,
    PasswordParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/password-parameter.model";
import {
    NumberParameterDescriptor,
    NumberParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/number-parameter.model";
import {
    DecimalParameterDescriptor,
    DecimalParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/decimal-parameter.model";
import {
    BooleanParameterDescriptor,
    BooleanParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/boolean-parameter.model";
import {
    FieldNameParameterDescriptor,
    FieldNameParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/field-name-parameter.model";
import {
    FieldNamePatternParameterDescriptor,
    PatternTypeChoice,
    FieldNamePatternParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/field-name-pattern-parameter.model";
import {
    FieldParameterDescriptor,
    FieldParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/field-parameter.model";
import {
    TextListParameterDescriptor,
    TextListParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/text-list-parameter.model";
import {
    FieldNameListParameterDescriptor,
    FieldNameListParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/field-name-list-parameter.model";
import {
    FieldListParameterDescriptor,
    FieldListParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/field-list-parameter.model";
import {
    ChoiceParameterDescriptor,
    ChoiceParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/choice-parameter.model";
import {MaterialModule} from "@keyscore-manager-material/src/main/material.module";
import {
    ExpressionType,
    FieldNameHint,
    PatternType,
    FieldValueType
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-fields.model";
import {TextValue, MimeType} from "@/../modules/keyscore-manager-models/src/main/dataset/Value";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {of} from "rxjs";
import {NgModule} from "@angular/core";
import {ParameterGroupComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/parameter-group/parameter-group.component";
import {
    ParameterGroup,
    ParameterGroupDescriptor,
    BooleanParameterCondition
} from "@keyscore-manager-models/src/main/parameters/group-parameter.model";
import {PasswordParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/password-parameter/password-parameter.component";
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {ParameterGroupModule} from "@keyscore-manager-pipeline-parameters/src/main/parameters/parameter-group/parameter-group.module";

const staticTranslateLoader: TranslateLoader = {
    getTranslation(lang: string) {
        return of(require('../../../../public/assets/i18n/en.json'))
    }
};

@NgModule()
class I18nModule {
    constructor(translate: TranslateService) {
        translate.setDefaultLang('en');
        translate.use('en');
    }
}

storiesOf('Parameters/ExpressionParameter', module)
    .addDecorator(
        moduleMetadata({
            declarations: [],
            imports: [
                CommonModule,
                MaterialModule,
                BrowserAnimationsModule,
                I18nModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useValue: staticTranslateLoader
                    }
                })
                // ExpressionParameterModule
            ],
            providers: []
        }))
    .add("with three expression types", () => ({
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
    })).add("with one expression type", () => ({
    component: ExpressionParameterComponent,
    props: {
        descriptor: new ExpressionParameterDescriptor({id: "myexpression"},
            "Field Pattern", "", "", true, [
                new ExpressionParameterChoice("expression.regex", "RegEx", ""),
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
            BrowserAnimationsModule,
            I18nModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useValue: staticTranslateLoader
                }
            })
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

storiesOf('Parameters/PasswordParameter', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule,
            I18nModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useValue: staticTranslateLoader
                }
            })
        ],
        providers: [StringValidatorService]
    }))
    .add("default", () => ({
        component: PasswordParameterComponent,
        props: {
            descriptor: new PasswordParameterDescriptor({id: "mypassword"},
                "Password", "", "", null, 8, 12, true)
            ,
            parameter: new PasswordParameter({id: "mypassword"}, "changeme",),
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
            SharedControlsModule,
            I18nModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useValue: staticTranslateLoader
                }
            })
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
            SharedControlsModule,
            I18nModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useValue: staticTranslateLoader
                }
            })
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
            SharedControlsModule,
            I18nModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useValue: staticTranslateLoader
                }
            })
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
                SharedControlsModule,
                I18nModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useValue: staticTranslateLoader
                    }
                })
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
                SharedControlsModule,
                I18nModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useValue: staticTranslateLoader
                    }
                })
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
            value: new TextValue("initial value", MimeType.TEXT_PLAIN)
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
            ListParameterModule,
            I18nModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useValue: staticTranslateLoader
                }
            })
        ],
        providers: [ParameterComponentFactoryService, ParameterFactoryService]
    })).add("Text", () => {
    return {
        template: `<parameter-list [descriptor]="descriptor" [parameter]="parameter" (parameter)="emitter($event)"></parameter-list>`,
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
        template: `<parameter-list [descriptor]="descriptor" [parameter]="parameter" [autoCompleteDataList]="autoCompleteDataList" (parameter)="emitter($event)"></parameter-list>`,
        props: {
            descriptor: new FieldNameListParameterDescriptor({id: "fieldNameList"}, "FieldName List Parameter",
                "List of messages to add", new FieldNameParameterDescriptor({id: "myFieldNameParameter"}, "Field Name Parameter",
                    "My field name Parameter", "Fieldname", FieldNameHint.PresentField, null, false), 2, 5),
            parameter: new FieldNameListParameter({id: "myFieldNameParameter"}, ["message", "robo_time"]),
            autoCompleteDataList: ['message', 'timestamp', 'robo_time', 'logbee_time', 'logbee_id'],
            emitter: action('Value Change')
        }
    }
}).add("Field", () => {
    return {
        template: `<parameter-list [descriptor]="descriptor" [parameter]="parameter" [autoCompleteDataList]="autoCompleteDataList" (parameter)="emitter($event)"></parameter-list>`,
        props: {
            descriptor: new FieldListParameterDescriptor({id: "fieldList"}, "Field List Parameter",
                "List of messages to add", new FieldParameterDescriptor({id: "myFieldParameter"},
                    "Field", "", "", FieldNameHint.AnyField, null, FieldValueType.Timestamp, false), 2, 5),
            parameter: new FieldListParameter({id: "fieldList"}, []),
            autoCompleteDataList: ['message', 'timestamp', 'robo_time', 'logbee_time', 'logbee_id'],
            emitter: action('Value Change')
        }
    }
});

storiesOf('Parameters/ChoiceParameter', module)
    .addDecorator(
        moduleMetadata({
            declarations: [],
            imports: [
                CommonModule,
                MaterialModule,
                BrowserAnimationsModule,
                ReactiveFormsModule,
                I18nModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useValue: staticTranslateLoader
                    }
                })
            ],
            providers: []
        }))
    .add("Single Choice", () => ({
        component: ChoiceParameterComponent,
        props: {
            descriptor: new ChoiceParameterDescriptor({id: 'choiceParameter'}, "Choice Parameter",
                "Some Choices here", 1, 1, [
                    {name: 'choice1', displayName: 'Choice 1', description: ''},
                    {name: 'choice2', displayName: 'Choice 2', description: 'This one has a description'},
                    {name: 'choice3', displayName: 'Choice 1', description: ''}
                ])
            ,
            parameter: new ChoiceParameter({id: "choiceParameter"}, ""),
            emitter: action('Value Change')
        }
    })).add("Multiple Choices", () => ({
    component: ChoiceParameterComponent,
    props: {
        descriptor: new ChoiceParameterDescriptor({id: 'choiceParameter'}, "Choice Parameter",
            "Some Choices here", 2, 3, [
                {name: 'choice1', displayName: 'Choice 1', description: ''},
                {name: 'choice2', displayName: 'Choice 2', description: 'This one has a description'},
                {name: 'choice3', displayName: 'Choice 3', description: ''},
                {name: 'choice4', displayName: 'Choice 4', description: ''},
                {name: 'choice5', displayName: 'Choice 5', description: ''},
                {name: 'choice6', displayName: 'Choice mit ganaaaaaaanz langem text', description: ''},

            ])
        ,
        parameter: new ChoiceParameter({id: "choiceParameter"}, ""),
        emitter: action('Value Change')
    }
}));

storiesOf('Parameters/ParameterGroup', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule,
            TextParameterModule,
            I18nModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useValue: staticTranslateLoader
                }
            })
        ],
        providers: [
            ParameterComponentFactoryService,
            StringValidatorService
        ]
    })).add('default', () => ({
    component: ParameterGroupComponent,
    props: {
        descriptor: new ParameterGroupDescriptor({id: 'testGroup'}, 'Group', '', null, [
            new TextParameterDescriptor({id: 'groupText'}, 'GroupText', '', '', null, true),
            new TextParameterDescriptor({id: 'groupText2'}, 'GroupText2', '', '', null, false),
        ]),
        parameter: new ParameterGroup({id: 'testGroup'}, [
            new TextParameter({id: 'groupText'}, ''),
            new TextParameter({id: 'groupText2'}, 'init text'),
        ]),
        emitter: action('Value Change')
    }
})).add('without display name', () => ({
    component: ParameterGroupComponent,
    props: {
        descriptor: new ParameterGroupDescriptor({id: 'testGroup'}, '', '', null, [
            new TextParameterDescriptor({id: 'groupText'}, 'GroupText', '', '', null, true),
            new TextParameterDescriptor({id: 'groupText2'}, 'GroupText2', '', '', null, false),
        ]),
        parameter: new ParameterGroup({id: 'testGroup'}, [
            new TextParameter({id: 'groupText'}, ''),
            new TextParameter({id: 'groupText2'}, 'init text'),
        ]),
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
            NumberParameterModule,
            BooleanParameterModule,
            FieldParameterModule,
            ListParameterModule,
            ParameterGroupModule,
            I18nModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useValue: staticTranslateLoader
                }
            })
        ],
        providers: [
            ParameterComponentFactoryService,
            StringValidatorService
        ]
    })).add("default", () => ({
    component: ParameterFormComponent,
    props: {
        config: {
            id: 'testID',
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
                    'groupParameter':[
                        new ParameterGroup({id:'group'},[
                            new TextParameter({id:'textParameterGroup'},'ein text'),
                            new TextParameter({id:'textParameterGroup2'},'ein text1'),
                            new TextParameter({id:'textParameterGroup3'},'ein text2')
                        ]),
                        new ParameterGroupDescriptor({id:'group'},'Group','',new BooleanParameterCondition({id:'booleanParameter'},false),
                            [
                                new TextParameterDescriptor({id: 'textParameterGroup'}, "Text Parameter", "", "", null, true),
                                new TextParameterDescriptor({id: 'textParameterGroup2'}, "Text Parameter", "", "", null, true),
                                new TextParameterDescriptor({id: 'textParameterGroup3'}, "Text Parameter", "", "", null, true)
                            ])
                    ],
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
                }
            },
        },
        autoCompleteDataList: ['message', 'timestamp', 'robo_time', 'logbee_time'],
        onValueChange: action('Value changed')

    }
}));
