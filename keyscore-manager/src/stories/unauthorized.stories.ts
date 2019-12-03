import {storiesOf, moduleMetadata} from "@storybook/angular";
import {CommonModule} from "@angular/common";
import {MaterialModule} from '@keyscore-manager-material/src/main/material.module'
import {UnauthorizedComponent} from "@/app/common/unauthorized/unauthorized.component";
import {HttpClient} from "@angular/common/http";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {of} from "rxjs";
import {NgModule} from "@angular/core";

export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http);
}

const staticTranslateLoader: TranslateLoader = {
    getTranslation(lang: string) {
        return of(require('../../public/assets/i18n/en.json'))
    }
};

@NgModule()
class I18nModule {
    constructor(translate: TranslateService) {
        translate.setDefaultLang('en');
        translate.use('en')
    }
}

storiesOf('Unauthorized Component', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            I18nModule,
            CommonModule,
            MaterialModule,
            TranslateModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useValue: staticTranslateLoader,
                    useFactory: HttpLoaderFactory,
                    deps: [HttpClient]
                }
            })
        ],
        providers: []
    }))
    .add('default', () => ({
        component: UnauthorizedComponent,
        props: {}
    }));
