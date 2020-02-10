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
    ExpressionParameter,
    ExpressionParameterChoice,
    ExpressionParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/expression-parameter.model";
import {
    TextParameter,
    TextParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/text-parameter.model";
import {
    PasswordParameter,
    PasswordParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/password-parameter.model";
import {
    NumberParameter,
    NumberParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/number-parameter.model";
import {
    DecimalParameter,
    DecimalParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/decimal-parameter.model";
import {
    BooleanParameter,
    BooleanParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/boolean-parameter.model";
import {
    FieldNameParameter,
    FieldNameParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/field-name-parameter.model";
import {
    FieldNamePatternParameter,
    FieldNamePatternParameterDescriptor,
    PatternTypeChoice
} from "@/../modules/keyscore-manager-models/src/main/parameters/field-name-pattern-parameter.model";
import {
    FieldParameter,
    FieldParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/field-parameter.model";
import {
    TextListParameter,
    TextListParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/text-list-parameter.model";
import {
    FieldNameListParameter,
    FieldNameListParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/field-name-list-parameter.model";
import {
    FieldListParameter,
    FieldListParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/field-list-parameter.model";
import {
    ChoiceParameter,
    ChoiceParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/choice-parameter.model";
import {MaterialModule} from "@keyscore-manager-material/src/main/material.module";
import {
    ExpressionType,
    FieldNameHint,
    FieldValueType,
    PatternType
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-fields.model";
import {MimeType, TextValue} from "@/../modules/keyscore-manager-models/src/main/dataset/Value";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {of} from "rxjs";
import {NgModule} from "@angular/core";
import {ParameterGroupComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/parameter-group/parameter-group.component";
import {
    BooleanParameterCondition,
    ParameterGroup,
    ParameterGroupDescriptor
} from "@keyscore-manager-models/src/main/parameters/group-parameter.model";
import {PasswordParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/password-parameter/password-parameter.component";
import {ParameterGroupModule} from "@keyscore-manager-pipeline-parameters/src/main/parameters/parameter-group/parameter-group.module";
import {DirectiveComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/directive/directive.component";
import {
    FieldDirectiveDescriptor,
    FieldDirectiveSequenceParameterDescriptor,
    FieldDirectiveSequenceParameter,
    FieldDirectiveSequenceConfiguration
} from '@keyscore-manager-models/src/main/parameters/directive.model'
import {DirectiveSequenceComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/directive-sequence/directive-sequence.component";
import {FieldNameParameterModule} from "@keyscore-manager-pipeline-parameters/src/main/parameters/field-name-parameter/field-name-parameter.module";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {AddDirectiveComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/add-directive/add-directive.component";
import {IconEncoding, IconFormat} from "@keyscore-manager-models/src/main/descriptors/Icon";
import {DirectiveSequenceParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/directive-sequence-parameter.component";
import {ParameterErrorWrapperComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameter-error-wrapper.component";
import { boolean, number, text, withKnobs } from '@storybook/addon-knobs';


const staticTranslateLoader: TranslateLoader = {
    getTranslation(lang: string) {
        return of(require('../../../../public/assets/i18n/en.json'))
    }
};

const splitIcon: string = '<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 1000 1000" xml:space="preserve">\n' +
    '<g><g transform="translate(0.000000,511.000000) scale(0.100000,-0.100000)"><path d="M4240.3,3782.5c-112.6-39.7-187.6-110.4-214.1-207.5c-13.2-48.6-22.1-642.2-22.1-1540.5V571.4h-849.7h-849.7v465.7v465.7l-66.2,66.2c-57.4,55.2-81.7,66.2-141.2,55.2c-53-8.8-361.9-220.7-1010.8-690.8c-512-375.2-944.6-695.2-960-715C111,200.6,100,149.9,100,107.9c0-41.9,11-92.7,26.5-112.6c15.4-17.7,448-339.9,964.4-715c840.9-611.3,946.8-682,1024-682c66.2,0,94.9,13.3,136.8,59.6c53,61.8,53,68.4,53,525.3v461.3h849.7h849.7v-1461c0-896,8.8-1494.1,22.1-1542.7c81.7-293.5,534.1-293.5,620.2,2.2c13.2,50.8,19.9,1218.2,15.4,3513.5c-6.6,3429.6-6.6,3436.2-53,3495.8C4509.5,3787,4381.5,3831.1,4240.3,3782.5z"/><path d="M5707.9,3793.6c-90.5-19.9-176.6-97.1-216.3-192c-46.4-105.9-46.4-6881.3,0-6987.2c101.5-240.6,434.8-264.8,580.4-44.1c37.5,55.2,39.7,169.9,46.3,1566.9l6.6,1507.3h781.3h783.5v-465.7v-465.7l66.2-66.2c57.4-55.2,81.6-66.2,141.3-55.2c50.7,8.8,357.5,218.5,975.4,666.5C9369.2-382,9801.8-59.8,9837.1-28.9c83.9,79.4,83.9,194.2,0,273.7c-35.3,30.9-467.9,353.1-964.4,712.8c-613.5,443.6-926.9,659.9-975.4,666.5c-59.6,11-83.9,0-141.3-55.2l-66.2-66.2v-465.7V571.4h-783.5H6125l-6.6,1509.6c-6.6,1496.3-6.6,1511.8-52.9,1573.6C5979.4,3769.3,5838.1,3822.3,5707.9,3793.6z"/></g></g>\n' +
    '</svg>';

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
                }),
                ExpressionParameterModule
            ],
            providers: []
        })).addDecorator(withKnobs)
    .add("with three expression types and error", () => ({
        component: ParameterErrorWrapperComponent,
        props: {
            descriptor: new ExpressionParameterDescriptor({id: "myexpression"},
                "Field Pattern", "", "", true, [
                    new ExpressionParameterChoice("expression.regex", "RegEx", ""),
                    new ExpressionParameterChoice("expression.grok", "Grok", ""),
                    new ExpressionParameterChoice("expression.glob", "Glob", "")
                ])
            ,
            parameter: new ExpressionParameter({id: "myexpression"}, "Hello World", "regex"),
            wasUpdated:boolean('wasUpdated',true),
            onValueChange: action('Value Change')
        }
    })).add("with one expression type", () => ({
    component: ParameterErrorWrapperComponent,
    props: {
        descriptor: new ExpressionParameterDescriptor({id: "myexpression"},
            "Field Pattern", "", "", true, [
                new ExpressionParameterChoice("expression.regex", "RegEx", ""),
            ])
        ,
        parameter: new ExpressionParameter({id: "myexpression"}, "Hello World", "regex"),
        onValueChange: action('Value Change')
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
                    PatternTypeChoice.fromPatternType(PatternType.ExactMatch),
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
        parameter: new ParameterGroup({id: 'testGroup'}, {
            parameters: [
                new TextParameter({id: 'groupText'}, '')
                ,
                new TextParameter({id: 'groupText2'}, 'init text'),
            ]
        }),
        emitter: action('Value Change')
    }
})).add('without display name', () => ({
    component: ParameterGroupComponent,
    props: {
        descriptor: new ParameterGroupDescriptor({id: 'testGroup'}, '', '', null, [
            new TextParameterDescriptor({id: 'groupText'}, 'GroupText', '', '', null, true),
            new TextParameterDescriptor({id: 'groupText2'}, 'GroupText2', '', '', null, false),
        ]),
        parameter: new ParameterGroup({id: 'testGroup'}, {
            parameters:
                [
                    new TextParameter({id: 'groupText'}, ''),
                    new TextParameter({id: 'groupText2'}, 'init text')
                ]
        }),
        emitter: action('Value Change')
    }
}))
;

storiesOf('Parameters/Directives/DirectiveComponent', module).addDecorator(
    moduleMetadata({
        declarations: [DirectiveComponent],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule,
            TextParameterModule,
        ],
        providers: [
            ParameterComponentFactoryService,
            StringValidatorService
        ]
    })).add('with Parameter', () => ({
    component: DirectiveComponent,
    props: {
        descriptor: new FieldDirectiveDescriptor(
            {uuid: 'testID'},
            'Trim Directive',
            'A description to explain something',
            [
                new TextParameterDescriptor({id: 'textParameter'}, 'text', '', '', null, false)
            ],
            {
                data: splitIcon,
                encoding: IconEncoding.RAW,
                format: IconFormat.SVG
            }
        ),
        configuration: {
            ref: {uuid: 'testID'},
            instance: {uuid: 'instanceID'},
            parameters: {
                parameters: [
                    new TextParameter({id: 'textParameter'}, '')
                ]
            }
        },
        onChange: action('Configuration Change'),
        onDelete: action('Delete')
    }
})).add('without Parameter', () => ({
    component: DirectiveComponent,
    props: {
        descriptor: new FieldDirectiveDescriptor(
            {uuid: 'testID'},
            'Trim Directive',
            'A description to explain something',
            [],
            null
        ),
        configuration: {
            ref: {uuid: 'testID'},
            instance: {uuid: 'instanceID'},
            parameters: {
                parameters: []
            }
        },
        onChange: action('Configuration Change'),
        onDelete: action('Delete')
    }
}));

const fieldDirectiveSequenceParameterDescriptor: FieldDirectiveSequenceParameterDescriptor = new FieldDirectiveSequenceParameterDescriptor(
    {id: 'sequenceParameter'},
    'Directives',
    'A description for the directive.',
    null,
    [
        new FieldNameParameterDescriptor({id: 'fieldName'}, 'Fieldname', '', '', FieldNameHint.PresentField, null, true)
    ],
    [
        new FieldDirectiveDescriptor({uuid: 'trimDirective'}, 'Trim', 'Trims something', [
            new TextParameterDescriptor({id: 'textParam'}, 'TextParameter', '', '', null, false)
        ], null),
        new FieldDirectiveDescriptor({uuid: 'splitDirective'}, 'Split', 'Splits something', [], {
            data: splitIcon,
            format: IconFormat.SVG,
            encoding: IconEncoding.RAW
        }),
        new FieldDirectiveDescriptor({uuid: 'findAndReplaceDirective'}, 'Find & Replace', '', [], null),
        new FieldDirectiveDescriptor({uuid: 'toNumber'}, 'To Number', '', [], null),
        new FieldDirectiveDescriptor({uuid: 'toDecimal'}, 'To Decimal', '', [], null),
        new FieldDirectiveDescriptor({uuid: 'toTimestampDirective'}, 'To Timestamp', 'Converts a text to a timestamp.', [], null)
    ],
    0, 0
);

const sequence1: FieldDirectiveSequenceConfiguration = {
    id: 'sequence1',
    parameters: {parameters: [
            new FieldNameParameter({id: 'fieldName'}, '')
        ]},
    directives: [{
        ref: {uuid: 'trimDirective'},
        instance: {uuid: 'firstInstance'},
        parameters: {
            parameters: [
                new TextParameter({id: 'textParam'}, 'initialText')
            ]
        }
    }, {
        ref: {uuid: 'trimDirective'},
        instance: {uuid: 'firstInstance1'},
        parameters: {
            parameters: [
                new TextParameter({id: 'textParam'}, 'test')
            ]
        }
    }]
};

storiesOf('Parameters/Directives/DirectiveSequenceComponent', module).addDecorator(
    moduleMetadata({
        declarations: [DirectiveComponent, DirectiveSequenceComponent, AddDirectiveComponent],
        imports: [
            CommonModule,
            MaterialModule,
            DragDropModule,
            BrowserAnimationsModule,
            TextParameterModule,
            FieldNameParameterModule,
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
    })).add('with Parameter', () => ({
    component: DirectiveSequenceComponent,
    props: {
        descriptor: fieldDirectiveSequenceParameterDescriptor,
        sequence:
        sequence1,
        autoCompleteDataList: ['message', 'robo_time', 'logbee_time'],
        onSequenceChange: action('Sequence Changed')

    }
}));

storiesOf('Parameters/Directives/AddDirectiveComponent', module).addDecorator(
    moduleMetadata({
        declarations: [AddDirectiveComponent],
        imports: [
            CommonModule,
            MaterialModule,
            DragDropModule,
            BrowserAnimationsModule,
            TextParameterModule,
            FieldNameParameterModule,
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
    })).add('with Menu', () => ({
    component: AddDirectiveComponent,
    props: {
        itemsToAdd: [{id: 'id0', displayName: 'Trim', description: 'A directive description'}, {
            id: 'id1',
            displayName: 'Split',
            icon: {
                data: splitIcon,
                encoding: IconEncoding.RAW,
                format: IconFormat.SVG
            }
        }],
        onAdd: action('Add')
    }
})).add('without menu', () => ({
    component: AddDirectiveComponent,
    props: {
        onAdd: action('Add')
    }
}));

storiesOf('Parameters/Directives/DirectiveSequenceParameter', module).addDecorator(
    moduleMetadata({
        declarations: [
            AddDirectiveComponent,
            DirectiveComponent,
            DirectiveSequenceComponent,
            DirectiveSequenceParameterComponent],
        imports: [
            CommonModule,
            MaterialModule,
            DragDropModule,
            BrowserAnimationsModule,
            TextParameterModule,
            FieldNameParameterModule,
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
    })).add('with Menu', () => ({
    component: DirectiveSequenceParameterComponent,
    props: {
        descriptor: fieldDirectiveSequenceParameterDescriptor,
        parameter: new FieldDirectiveSequenceParameter(
            fieldDirectiveSequenceParameterDescriptor.ref, [sequence1]),
        autoCompleteDataList:['robo_time','logbee_time','message','timestamp'],
        emitter:action('Parameter Change')
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
                    'groupParameter': [
                        new ParameterGroup({id: 'group'}, {
                            parameters: [
                                new TextParameter({id: 'textParameterGroup'}, 'ein text'),
                                new TextParameter({id: 'textParameterGroup2'}, 'ein text1'),
                                new TextParameter({id: 'textParameterGroup3'}, 'ein text2')
                            ]
                        }),
                        new ParameterGroupDescriptor({id: 'group'}, 'Group', '', new BooleanParameterCondition({id: 'booleanParameter'}, false),
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
``
