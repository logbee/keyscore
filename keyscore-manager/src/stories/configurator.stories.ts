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
    .add("default", () => ({
        component: ConfiguratorComponent,
        props: {
            config: {
                conf: {
                    ref: {uuid: 'testFilter'},
                    parent: null,
                    parameterSet: {
                        parameters: [new TextListParameter({id: 'testTextList'}, ['test1', 'test2'])]
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
                        0, 0)],
                    categories: []
                },
                uuid: 'blablalbla-blablalbla-blalal-blalala'
            }
        }
    }));