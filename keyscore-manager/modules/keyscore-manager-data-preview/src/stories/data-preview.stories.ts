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
// import {
//     ChangeType,
//     Dataset,
//     DatasetTableModel,
//     DatasetTableRecordModel,
//     DatasetTableRowModel,
//     DatasetTableRowModelData,
//     ValueJsonClass
// } from "../../build/keyscore-manager-models";
//

// const exampleDataset: Dataset = {
//     metaData: null,
//     records: [
//         {
//             fields: [
//                 {
//                     name: "message",
//                     value: {
//                         jsonClass: ValueJsonClass.TextValue,
//                         value: "testValue1"
//                     }
//                 },
//                 {
//                     name: "measurement",
//                     value: {
//                         jsonClass: ValueJsonClass.TextValue,
//                         value: "125.265"
//                     }
//                 }
//             ]
//         }
//     ]
// };
//



export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http);
}

const staticTranslateLoader: TranslateLoader = {
    getTranslation(lang:string) {
        return of(require('../../../../public/assets/i18n/en.json'))
    }
};
@NgModule()
class I18nModule{
    constructor(translate: TranslateService) {
        translate.setDefaultLang('en');
        translate.use('en')
    }
}

storiesOf('DataPreview', module).addDecorator(
    moduleMetadata({
        declarations: [DataPreviewComponent,LeftToRightNavigationControl,ValueType],
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
    .add("Basic data preview", () => ({
    component: DataPreviewComponent,
    props: {
        selectedBlock: "test",
        // inputTableModels: new Map<string, DatasetTableModel>().set("test", tableModels),
        // outputTableModels: new Map<string, DatasetTableModel>().set("test", tableModels)
    }
}));