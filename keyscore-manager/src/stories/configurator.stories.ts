import {MaterialModule} from "@keyscore-manager-material/src/main/material.module";
import {ConfiguratorComponent} from "@/app/pipelines/pipeline-editor/pipely/configurator/configurator.component";
import {moduleMetadata, storiesOf} from "@storybook/angular";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {
    TextListParameter,
    TextListParameterDescriptor
} from "@keyscore-manager-models/src/main/parameters/parameter-lists/text-list-parameter.model";
import {TextParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/text-parameter.model";
import {ReactiveFormsModule} from "@angular/forms";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {HttpClient} from "@angular/common/http";
import {HttpLoaderFactory} from "@/app/app.module";
import {ParameterModule} from "@keyscore-manager-pipeline-parameters/src/main/parameter.module";
import {
    FieldListParameter,
    FieldListParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/field-list-parameter.model";
import {Field} from "@/../modules/keyscore-manager-models/src/main/dataset/Field";
import {TextValue} from "@/../modules/keyscore-manager-models/src/main/dataset/Value";
import {FieldParameterDescriptor} from "@/../modules/keyscore-manager-models/src/main/parameters/field-parameter.model";
import {FieldNameHint, FieldValueType} from "@keyscore-manager-models/src/main/parameters/parameter-fields.model";
import {IconModule} from "@/app/icon.module";

storiesOf('Configurator', module)
    .addDecorator(
        moduleMetadata({
            declarations: [],
            imports: [
                CommonModule,
                MaterialModule,
                BrowserAnimationsModule,
                ReactiveFormsModule,
                ParameterModule,
                IconModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useFactory: HttpLoaderFactory,
                        deps: [HttpClient]
                    }
                })
            ],
            providers: []
        }))
    .add("with two lists", () => ({
        component: ConfiguratorComponent,
        props: {
            config: {
                conf: {
                    ref: {uuid: 'testFilter'},
                    parent: null,
                    parameterSet: {
                        parameters: [new TextListParameter({id: 'testTextList'}, ['test1', 'test2']),
                            new FieldListParameter({id: 'testFieldList'}, [new Field('message',new TextValue('haha'))])
                        ]
                    }
                },
                descriptor: {
                    ref: {uuid: 'testFilter'},
                    displayName: 'testFilter',
                    description: 'some fancy description',
                    previousConnection: null,
                    nextConnection: null,
                    parameters: [new TextListParameterDescriptor({id: 'testTextList'}, 'Test Text List', '',
                        new TextParameterDescriptor({id: 'testItem'}, 'item', '', '', null, false),
                        0, 0),
                    new FieldListParameterDescriptor({id:'testFieldList'},'Test Field List','',
                        new FieldParameterDescriptor({id:'testFieldItem'},'haha','','',FieldNameHint.AnyField,null,FieldValueType.Text,false),
                        0,0)
                    ],
                    categories: [],
                    maturity: "Official"
                },
                uuid: '18e8cec1-3500-4de3-965a-deea215a24c4'
            }
        }
    }));