import {PipelinesComponent} from "../../app/pipelines/pipelines.component";
import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {Subject} from "rxjs";
import {PipelineEditorComponent} from "../../app/pipelines/pipeline-editor/pipeline-editor.component";
import {RefreshTimeComponent} from "../../app/common/loading/refresh.component";
import {RouterTestingModule} from "@angular/router/testing";
import {Store} from "@ngrx/store";
import {HeaderBarModule} from "../../app/common/headerbar/headerbar.module";
import {MaterialModule} from "../../app/material.module";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {LoadingModule} from "../../app/common/loading/loading.module";
import {HealthModule} from "../../app/common/health/health.module";
import {PipelyModule} from "../../app/pipelines/pipeline-editor/pipely/pipely.module";
import {AlertModule} from "../../app/common/alert/alert.module";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {HttpLoaderFactory} from "../../app/app.module";
import {generateEditingPipelineModel} from "../fake-data/pipeline-fakes";
import {hot} from "jasmine-marbles";

import {LoadFilterDescriptorsAction, UpdatePipelineAction} from "../../app/pipelines/pipelines.actions";
import {PipelyKeyscoreAdapter} from "../../app/services/pipely-keyscore-adapter.service";
import {generateEmptyEditingPipelineModel} from "../../app/models/pipeline-model/EditingPipelineModel";


describe('PipelineEditorComponent', () => {
    let component: PipelineEditorComponent;
    let fixture: ComponentFixture<PipelineEditorComponent>;
    let unsubscribe = new Subject<void>();

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [
                PipelinesComponent,
                PipelineEditorComponent,
                RefreshTimeComponent
            ],
            imports: [
                RouterTestingModule,
                HeaderBarModule,
                MaterialModule,
                HttpClientModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useFactory: HttpLoaderFactory,
                        deps: [HttpClient]
                    }
                }),
                LoadingModule,
                HealthModule,
                PipelyModule,
                AlertModule
            ],
            providers: [
                {
                    provide: PipelyKeyscoreAdapter,
                    useValue: {
                        resolvedParameterDescriptorToBlockDescriptor: jest.fn()
                    }
                },
                {
                    provide: Store,
                    useValue: {
                        dispatch: jest.fn(),
                        pipe: jest.fn()
                    }
                }
            ]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(PipelineEditorComponent);
        component = fixture.componentInstance;
    });

    afterEach(() => {
        unsubscribe.next();
        unsubscribe.complete();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit()', () => {
        it('should dispatch an LoadFilterDescriptor action', () => {
            const action = new LoadFilterDescriptorsAction();
            const store = TestBed.get(Store);
            const spy = jest.spyOn(store,'dispatch');
            store.pipe = jest.fn(() => hot('-'));

            fixture.detectChanges();

            expect(spy).toHaveBeenCalledWith(action);
        })
    });

    describe('updatePipeline',() => {
        it('should dispatch an UpdatePipeline action with the complete pipeline on pipeline creation',()=>{
            const updatedModel = generateEditingPipelineModel(10);
            const action = new UpdatePipelineAction(updatedModel);
            const store = TestBed.get(Store);

            const spy = jest.spyOn(store,'dispatch');

            component.storeEditingPipeline = generateEmptyEditingPipelineModel();
            component.updatePipeline(updatedModel);

            expect(spy).toHaveBeenCalledWith(action);
        });

        it('should dispatch an UpdatePipeline action with only the changed configurations on updating configurations',() =>{
            const differenceModel = require('../fake-data/difference-pipeline-model-for-config-update-test.json');
            component.storeEditingPipeline = require('../fake-data/pipeline-model-for-config-update-test.json');
            const updatedModel = require('../fake-data/updated-pipeline-model-for-config-update-test.json');
            const store = TestBed.get(Store);

            const spy = jest.spyOn(store,'dispatch');
            const action = new UpdatePipelineAction(differenceModel);

            component.updatePipeline(updatedModel);

            expect(spy).toHaveBeenCalledWith(action);
        });

        it(`should dispatch an UpdatePipeline action with only the changed blueprints and added configuration
        on rearranging and adding pipeline elements `,() =>{
            const differenceModel = require('../fake-data/difference-pipeline-model-for-rearrange-update-test.json');
            component.storeEditingPipeline = require('../fake-data/pipeline-model-for-rearrange-update-test.json');
            const updatedModel = require('../fake-data/updated-pipeline-model-for-rearrange-update-test.json');
            const store = TestBed.get(Store);

            const spy = jest.spyOn(store,'dispatch');
            const action = new UpdatePipelineAction(differenceModel);

            component.updatePipeline(updatedModel);

            expect(spy).toHaveBeenCalledWith(action);
        });
    })

});