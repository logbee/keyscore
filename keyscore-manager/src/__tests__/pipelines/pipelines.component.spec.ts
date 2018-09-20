import {PipelinesComponent} from "../../app/pipelines/pipelines.component";
import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {Subject} from "rxjs";
import {PipelineEditorComponent} from "../../app/pipelines/pipeline-editor/pipeline-editor.component";
import {PipelyComponent} from "../../app/pipelines/pipeline-editor/pipely/pipely.component";
import {RefreshTimeComponent} from "../../app/common/loading/refresh.component";
import {RouterTestingModule} from "@angular/router/testing";
import {Store} from "@ngrx/store";
import {HeaderBarModule} from "../../app/common/headerbar.module";
import {MaterialModule} from "../../app/material.module";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {LoadingModule} from "../../app/common/loading/loading.module";
import {HealthModule} from "../../app/common/health/health.module";
import {PipelyModule} from "../../app/pipelines/pipeline-editor/pipely/pipely.module";
import {AlertModule} from "../../app/common/alert/alert.module";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {HttpLoaderFactory} from "../../app/app.module";
import {generatePipelines} from "../fake-data/pipeline-fakes";
import {cold, hot} from "jasmine-marbles";

import {LoadAllPipelinesAction, UpdatePipelinePollingAction} from "../../app/pipelines/pipelines.actions";

describe('PipelinesComponent', () => {
    let component: PipelinesComponent;
    let fixture: ComponentFixture<PipelinesComponent>;
    let unsubscribe = new Subject<void>();

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [
                PipelinesComponent,
                PipelineEditorComponent,
                PipelyComponent,
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
        fixture = TestBed.createComponent(PipelinesComponent);
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
        it('should select all pipelines', () => {
            const store = TestBed.get(Store);
            const pipelines = generatePipelines();
            store.pipe = jest.fn(() => hot('-a', {a: pipelines}));

            fixture.detectChanges();

            const expected = cold('-a', {a: pipelines});
            expect(component.pipelines$).toBeObservable(expected);
        });

        it('should dispatch UpdatePipelinePolling action in OnInit lifecycle', () => {
            const action = new UpdatePipelinePollingAction(true);
            const store = TestBed.get(Store);
            const spy = jest.spyOn(store, 'dispatch');

            fixture.detectChanges();

            expect(spy).toHaveBeenCalledWith(action);
        });

        it('should dispatch LoadAllPipelines action in OnInit lifecycle', () => {
            const action = new LoadAllPipelinesAction();
            const store = TestBed.get(Store);
            const spy = jest.spyOn(store, 'dispatch');

            fixture.detectChanges();

            expect(spy).toHaveBeenCalledWith(action);
        });
    });

    describe('createPipeline()', () => {
        describe('given truthy activeRouting', () => {
            it('should dispatch two actions', () => {
                const store = TestBed.get(Store);
                const spy = jest.spyOn(store, 'dispatch');
                component.createPipeline(true);
                expect(spy).toHaveBeenCalledTimes(2);
                //expect(spy).nthCalledWith(1,expect.any(CreatePipelineAction));
                //expect(spy).nthCalledWith(2,expect.any(RouterActions.Go));

            });
        });
        describe('given false activeRouting', () => {
            it('should dispatch only one action', () => {
                const store = TestBed.get(Store);
                const spy = jest.spyOn(store, 'dispatch');
                component.createPipeline(false);
                expect(spy).toHaveBeenCalledTimes(1);
                //expect(spy).toHaveBeenCalledWith(expect.any(CreatePipelineAction));
            })
        })
    })


});