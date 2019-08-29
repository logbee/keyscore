import {empty, Observable} from "rxjs";
import {Actions} from "@ngrx/effects";
import {PipelinesEffects} from "../../../app/pipelines/effects/pipelines.effects";
import {TestBed} from "@angular/core/testing";
import {Store} from "@ngrx/store";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {HttpLoaderFactory} from "../../../app/app.module";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {
    generateBlueprint,
    generateBlueprints,
    generateConfiguration,
    generateEditingPipelineModel,
    generatePipelineBlueprint
} from "@keyscore-manager-test-fixtures";
import {
    EditPipelineAction,
    EditPipelineFailureAction,
    EditPipelineSuccessAction,
    LoadEditBlueprintsAction,
    LoadEditPipelineConfigAction,
    LoadFilterDescriptorsAction,
    LoadFilterDescriptorsFailureAction,
    LoadFilterDescriptorsSuccessAction,
    ResolveFilterDescriptorSuccessAction,
    UpdatePipelineAction,
    UpdatePipelineFailureAction,
    UpdatePipelineSuccessAction
} from "../../../app/pipelines/actions/pipelines.actions";
import {cold, hot} from "jest-marbles";
import {
    removeFieldFilterDescriptorJson,
    resolvedRemoveFieldsFilterDE
} from "./resolved-remove-fields-filter-descriptor";
import {Descriptor,ResolvedFilterDescriptor} from "@keyscore-manager-models";
import {DescriptorResolverService} from "../../../app/services/descriptor-resolver.service";
import {BlueprintService} from "../../../app/services/rest-api/BlueprintService";
import {Error} from "tslint/lib/error";
import {ConfigurationService} from "../../../app/services/rest-api/ConfigurationService";
import {DescriptorService} from "../../../app/services/rest-api/DescriptorService";

export class TestActions extends Actions {
    constructor() {
        super(empty());
    }

    set stream(source: Observable<any>) {
        this.source = source;
    }
}

export function getActions() {
    return new TestActions();
}

describe('PipelinesEffects', () => {
    let actions: TestActions;
    let effects: PipelinesEffects;
    let blueprintService: BlueprintService;
    let configurationService: ConfigurationService;
    let descriptorService: DescriptorService;
    let resolverService: DescriptorResolverService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                HttpClientModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useFactory: HttpLoaderFactory,
                        deps: [HttpClient]
                    }
                })
            ],
            providers: [
                PipelinesEffects,
                {
                    provide: Actions,
                    useFactory: getActions
                },
                {
                    provide: BlueprintService,
                    useValue: {
                        getPipelineBlueprint: jest.fn(),
                        getBlueprint: jest.fn(),
                        getConfiguration: jest.fn(),
                        putPipelineBlueprint: jest.fn(),
                        putConfiguration: jest.fn(),
                        putBlueprint: jest.fn(),
                        getAllDescriptors: jest.fn()
                    }
                },
                {
                    provide: Store,
                    useValue: {
                        dispatch: jest.fn(),
                        pipe: jest.fn(),
                        select: jest.fn()
                    }
                },
                {
                    provide: DescriptorResolverService,
                    useValue: {
                        resolveDescriptor: jest.fn()
                    }
                }
            ]
        });

        actions = TestBed.get(Actions);
        effects = TestBed.get(PipelinesEffects);
        blueprintService = TestBed.get(BlueprintService);
        resolverService = TestBed.get(DescriptorResolverService);
    });

    it('should be created', () => {
        expect(effects).toBeTruthy();
    });

    describe('getEditPipelineBlueprint', () => {
        it('should return an LoadEditBlueprints action, on success', () => {
            const pipelineBlueprint = generatePipelineBlueprint();
            const action = new EditPipelineAction(pipelineBlueprint.ref.uuid);
            const outcome = new LoadEditBlueprintsAction(pipelineBlueprint);

            actions.stream = hot('-a', {a: action});
            const response = cold('-a|', {a: pipelineBlueprint});
            const expected = cold('--b', {b: outcome});
            blueprintService.getPipelineBlueprint = jest.fn(() => response);

            expect(effects.loadEditPipelineBlueprint$).toBeObservable(expected);

        });

        it('should return an EditPipelineSuccess action with empty blueprint and config list, when no blueprints$ are defined', () => {
            const pipelineBlueprint = generatePipelineBlueprint(0);
            const action = new EditPipelineAction(pipelineBlueprint.ref.uuid);
            const outcome = new EditPipelineSuccessAction(pipelineBlueprint, [], []);

            actions.stream = hot('-a', {a: action});
            const response = cold('-a|', {a: pipelineBlueprint});
            const expected = cold('--b', {b: outcome});
            blueprintService.getPipelineBlueprint = jest.fn(() => response);

            expect(effects.loadEditPipelineBlueprint$).toBeObservable(expected);

        });

        it('should return an EditPipelineFailure action with the pipeline id and an error, on failure', () => {
            const pipelineBlueprint = generatePipelineBlueprint();
            const action = new EditPipelineAction(pipelineBlueprint.ref.uuid);
            const error = new Error();
            const outcome = new EditPipelineFailureAction(error);

            actions.stream = hot('-a', {a: action});
            const response = cold('-#|', {}, error);
            const expected = cold('--b', {b: outcome});
            blueprintService.getPipelineBlueprint = jest.fn(() => response);

            expect(effects.loadEditPipelineBlueprint$).toBeObservable(expected);
        })
    });

    describe('loadEditBlueprints', () => {
        it('should return an LoadEditPipelineConfig action containing the loaded blueprints$', () => {
            const pipelineBlueprint = generatePipelineBlueprint(3);
            const blueprint = generateBlueprint();
            const blueprints = [blueprint, blueprint, blueprint];
            const action = new LoadEditBlueprintsAction(pipelineBlueprint);
            const outcome = new LoadEditPipelineConfigAction(pipelineBlueprint, blueprints);

            actions.stream = hot('-a', {a: action});
            const response = cold('-a|', {a: blueprint});
            const expected = cold('---b', {b: outcome});
            blueprintService.getBlueprint = jest.fn(() => response);

            expect(effects.loadEditBlueprints$).toBeObservable(expected);
        });

        it('should return an EditPipelineFailure action, on failure', () => {
            const pipelineBlueprint = generatePipelineBlueprint();
            const action = new LoadEditBlueprintsAction(pipelineBlueprint);
            const error = new Error();
            const outcome = new EditPipelineFailureAction(error);

            actions.stream = hot('-a', {a: action});
            const responseGetBlueprint = cold('-#|', {}, error);
            const expected = cold('--b', {b: outcome});
            blueprintService.getBlueprint = jest.fn(() => responseGetBlueprint);

            expect(effects.loadEditBlueprints$).toBeObservable(expected);
        });
    });
    describe('getConfigurations', () => {
        it('should return an EditPipelineSuccess action containing pipelineBlueprint,blueprints$ and configs', () => {
            const pipelineBlueprint = generatePipelineBlueprint(3);
            const blueprints = generateBlueprints(3);
            const configuration = generateConfiguration();
            const configurations = [configuration, configuration, configuration];
            const action = new LoadEditPipelineConfigAction(pipelineBlueprint, blueprints);
            const outcome = new EditPipelineSuccessAction(pipelineBlueprint, blueprints, configurations);

            actions.stream = hot('-a', {a: action});
            const response = cold('-a|', {a: configuration});
            const expected = cold('---b', {b: outcome});
            configurationService.getConfiguration = jest.fn(() => response);

            expect(effects.loadEditConfigs$).toBeObservable(expected);
        });


        it('should return an EditPipelineFailure action with pipelineid and error, on failure', () => {
            const pipelineBlueprint = generatePipelineBlueprint();
            const blueprints = generateBlueprints(10);
            const action = new LoadEditPipelineConfigAction(pipelineBlueprint, blueprints);
            const error = new Error();
            const outcome = new EditPipelineFailureAction(error);

            actions.stream = hot('-a', {a: action});
            const response = cold('-#|', {}, error);
            const expected = cold('--b', {b: outcome});
            configurationService.getConfiguration = jest.fn(() => response);

            expect(effects.loadEditConfigs$).toBeObservable(expected);
        })
    });
    describe('loadFilterDescriptors', () => {
        it('should return an LoadFilterDescriptorsSuccess action with the descriptor, on success', () => {
            const descriptor: Descriptor = JSON.parse(removeFieldFilterDescriptorJson);
            const descriptors: Descriptor[] = [descriptor];
            const action = new LoadFilterDescriptorsAction();
            const outcome = new LoadFilterDescriptorsSuccessAction(descriptors);

            actions.stream = hot('-a', {a: action});
            const response = cold('-a|', {a: descriptors});
            const expected = cold('--b', {b: outcome});
            descriptorService.getAllDescriptors = jest.fn(() => response);

            expect(effects.loadFilterDescriptors$).toBeObservable(expected);
        });
        it('should return an LoadFilterDescriptorsFailure action with the error cause on failure', () => {
            const action = new LoadFilterDescriptorsAction();
            const error = new Error();
            const outcome = new LoadFilterDescriptorsFailureAction(error);

            actions.stream = hot('-a', {a: action});
            const response = cold('-#|', {}, error);
            const expected = cold('--b', {b: outcome});
            descriptorService.getAllDescriptors = jest.fn(() => response);

            expect(effects.loadFilterDescriptors$).toBeObservable(expected);

        });

    });
    describe('resolveFilterDescriptors', () => {
        it('should return an ResolveFilterDescriptorSuccess action on success', () => {
            const descriptors: Descriptor[] = [JSON.parse(removeFieldFilterDescriptorJson)];
            const resolvedDescriptor: ResolvedFilterDescriptor = resolvedRemoveFieldsFilterDE;

            const action = new LoadFilterDescriptorsSuccessAction(descriptors);
            const outcome = new ResolveFilterDescriptorSuccessAction([resolvedDescriptor]);

            actions.stream = hot('-a', {a: action});
            const expected = cold('-b', {b: outcome});
            resolverService.resolveDescriptor = jest.fn(() => resolvedDescriptor);

            expect(effects.resolveFilterDescriptors$).toBeObservable(expected);

        })
    });

    describe('updatePipeline', () => {
        it(`should return an UpdatePipelineSuccessAction and call putBlueprint and putConfig for each
        blueprint and each config in the model`, () => {
            const pipeline = generateEditingPipelineModel();
            const action = new UpdatePipelineAction(pipeline);
            const outcome = new UpdatePipelineSuccessAction(pipeline,action.runAfterUpdate);


            actions.stream = hot('-a', {a: action});
            const servicesResponse = cold('-a|', {a: {}});
            const expected = cold('---b', {b: outcome});
            blueprintService.putPipelineBlueprint = jest.fn(() => servicesResponse);
            blueprintService.putBlueprint = jest.fn(() => servicesResponse);
            configurationService.putConfiguration = jest.fn(() => servicesResponse);

            const configUpdateSpy = jest.spyOn(configurationService, 'putConfiguration');
            const blueprintUpdateSpy = jest.spyOn(blueprintService, "putBlueprint");
            const pipelineBlueprintUpdateSpy = jest.spyOn(blueprintService, "putPipelineBlueprint");


            expect(effects.updatePipeline$).toBeObservable(expected);
            expect(configUpdateSpy).toHaveBeenCalledTimes(pipeline.configurations.length);
            expect(blueprintUpdateSpy).toHaveBeenCalledTimes(pipeline.blueprints.length);
            expect(pipelineBlueprintUpdateSpy).toHaveBeenCalledTimes(1);

        });
        it('should return an UpdatePipelineFailureAction if one ore more elements fail', () => {
            const pipeline = generateEditingPipelineModel();
            const action = new UpdatePipelineAction(pipeline);
            const error = new Error();
            const outcome = new UpdatePipelineFailureAction(error, pipeline);

            actions.stream = hot('-a', {a: action});
            const blueprintsServiceResponse = cold('-a|', {a: {}});
            const configServiceResponse = cold('-#|', {}, error);
            const expected = cold('--b', {b: outcome});
            blueprintService.putPipelineBlueprint = jest.fn(() => blueprintsServiceResponse);
            blueprintService.putBlueprint = jest.fn(() => blueprintsServiceResponse);
            configurationService.putConfiguration = jest.fn(() => configServiceResponse);

            expect(effects.updatePipeline$).toBeObservable(expected);
        });
    });


});
