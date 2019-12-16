import {moduleMetadata, storiesOf} from "@storybook/angular";
import {action} from '@storybook/addon-actions';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {CommonModule} from "@angular/common";
import {HttpClient} from "@angular/common/http";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {of} from "rxjs";
import {NgModule} from "@angular/core";
import {MaterialModule} from '@keyscore-manager-material/src/main/material.module';
import {PipelineOverviewComponent} from "@/app/pipelines/pipeline-overview/pipeline-overview.component";
import {PipelineTableModel} from "@/app/pipelines/PipelineTableModel";
import {Health} from "@keyscore-manager-models/src/main/common/Health";
import {DataSourceFactory} from "@/app/data-source/data-source-factory";
import {HealthModule} from "@/app/common/health/health.module";
import { boolean, number, text, withKnobs } from '@storybook/addon-knobs';


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

const mockPipelines: PipelineTableModel[] = [{
    uuid: '5657267b-68f7-4c91-947f-5f346dfc5511',
    health: Health.Green,
    name: 'firstPipeline',
    description: 'A first test pipeline'
},{
    uuid: 'b238dc0b-3975-4cde-b1a6-a295840a0b33',
    health: Health.Red,
    name: 'secondPipeline',
    description: 'A second test pipeline'
},{
    uuid: '68325f6c-6f0b-4ba7-9f89-ac4ed45838aa',
    health: Health.Yellow,
    name: 'thirdPipeline',
    description: 'A third test pipeline'
},{
    uuid: '5136e716-7c0e-422a-8196-3b47bcb9b6d4',
    health: Health.Unknown,
    name: 'fourthPipeline',
    description: 'A fourth test pipeline'
}];

const dataSourceFactory: DataSourceFactory = new DataSourceFactory();

storiesOf('PipelineOverviewTable', module)
    .addDecorator(withKnobs)
    .addDecorator(
        moduleMetadata({
            declarations: [PipelineOverviewComponent],
            imports: [
                I18nModule,
                BrowserAnimationsModule,
                MaterialModule,
                TranslateModule,
                CommonModule,
                HealthModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useValue: staticTranslateLoader,
                        useFactory: HttpLoaderFactory,
                        deps: [HttpClient]
                    }
                }),]
        }))
    .add('default', () => ({
        component: PipelineOverviewComponent,
        props: {
            dataSource: dataSourceFactory.createPipelineDataSource(mockPipelines),
            selectionMode: boolean('selectionMode', false),
            editPipeline: action('edit pipeline'),
            deployPipeline: action('deploy pipeline'),
            pipelinesSelected: action('pipelines selected')
        }
    }));