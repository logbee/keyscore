import {moduleMetadata, storiesOf} from "@storybook/angular";
import {DataPreviewComponent} from "../main/components/data-preview.component";
import {CommonModule} from "@angular/common";
import {MaterialModule} from "@keyscore-manager-material/src/main/material.module";
import {LeftToRightNavigationControl} from "../main/components/left-right-navigation-control.component";
import {ValueType} from "../main/components/value.type.component";
import {HttpClient} from "@angular/common/http";
import {TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {of} from "rxjs";
import {NgModule} from "@angular/core";

import {ValueJsonClass} from "@/../modules/keyscore-manager-models/src/main/dataset/Value";
import {Dataset} from "@/../modules/keyscore-manager-models/src/main/dataset/Dataset";


const exampleDataset: Dataset = {
    metaData: null,
    records: [
        {
            fields: [
                {
                    name: "user",
                    value: {
                        jsonClass: ValueJsonClass.TextValue,
                        value: "{ 'name': 'elmar' }",
                        mimetype: { primary: "application", sub: "json" }
                    }
                },
                {
                    name: "pin",
                    value: {
                        jsonClass: ValueJsonClass.NumberValue,
                        value: 1234,
                    }
                },
                {
                    name: "picture",
                    value: {
                        jsonClass: ValueJsonClass.BinaryValue,
                        value: new Uint8Array(0),
                        mimetype: { primary: "image", sub: "jpeg" }
                    }
                }
            ]
        },
        {
            fields: [
                {
                    name: "message",
                    value: {
                        jsonClass: ValueJsonClass.TextValue,
                        value: "testValue1",
                        mimetype: { primary: "text", sub: "plain" }
                    }
                },
                {
                    name: "condition",
                    value: {
                        jsonClass: ValueJsonClass.BooleanValue,
                        value: true,
                    }
                }
            ]
        }
    ]
};

const exampleDataset2: Dataset = {
    ...exampleDataset,
    records: [exampleDataset.records[0]]
};

const exampleDataset3: Dataset = {
    metaData: null,
    records: [
        {
            fields: [
                {
                    name: "message",
                    value: {
                        jsonClass: ValueJsonClass.TextValue,
                        value: "The weather is cloudy.",
                        mimetype: null
                    }
                },
                {
                    name: "temperature",
                    value: {
                        jsonClass: ValueJsonClass.DecimalValue,
                        value: 11.5,
                    }
                }
            ]
        },
        {
            fields: [
                {
                    name: "message",
                    value: {
                        jsonClass: ValueJsonClass.TextValue,
                        value: "It's a sunny day.",
                        mimetype: { primary: "text", sub: "plain" }
                    }
                },
                {
                    name: "temperature",
                    value: {
                        jsonClass: ValueJsonClass.DecimalValue,
                        value: 42.73,
                    }
                }
            ]
        }
    ]
};

let mapIn = new Map([
    ["test1", [exampleDataset, exampleDataset2,exampleDataset3]],
    ["test2", []],
    ["test3", [exampleDataset]]
]);
let mapOut = new Map([
    ["test1", [exampleDataset2,exampleDataset3]],
    ["test2", []],
    ["test3", [exampleDataset]]
]);



export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http);
}

const staticTranslateLoader: TranslateLoader = {
    getTranslation(lang: string) {
        return of(require('../../../../public/assets/i18n/en.json'))
    }
};

@NgModule()
class I18nModule {
    constructor(translate: TranslateService) {
        translate.setDefaultLang('en');
        translate.use('en')
    }
}

storiesOf('DataPreview', module).addDecorator(
    moduleMetadata({
        declarations: [DataPreviewComponent, LeftToRightNavigationControl, ValueType],
        imports: [
            I18nModule,
            BrowserAnimationsModule,
            MaterialModule,
            TranslateModule,
            CommonModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useValue: staticTranslateLoader,
                    useFactory: HttpLoaderFactory,
                    deps: [HttpClient]
                }
            }),]
    }))
    .add("Basic data preview with datasets", () => ({
        component: DataPreviewComponent,
        props: {
            selectedBlock: "test1",
            inputDatasets: mapIn,
            outputDatasets: mapOut
        }
    })).add("Data preview without datasets", () => ({
    component: DataPreviewComponent,
    props: {
        selectedBlock: "test2",
        inputDatasets: mapIn,
        outputDatasets: mapIn
    }
}));