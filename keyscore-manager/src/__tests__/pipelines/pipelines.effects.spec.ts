import {empty, Observable} from "rxjs/index";
import {Actions} from "@ngrx/effects";
import {PipelinesEffects} from "../../app/pipelines/pipelines.effects";
import {PipelineService} from "../../app/services/rest-api/pipeline.service";
import {TestBed} from "@angular/core/testing";
import {Store} from "@ngrx/store";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {HttpLoaderFactory} from "../../app/app.module";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {generatePipelineBlueprint} from "../fake-data/pipeline-fakes";
import {EditPipelineAction, LoadEditBlueprintsAction} from "../../app/pipelines/pipelines.actions";
import {cold, hot} from "jasmine-marbles";

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
    let pipelineService: PipelineService;

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
                    provide: PipelineService,
                    useValue: {
                        getPipelineBlueprint: jest.fn()
                    }
                },
                {
                    provide: Store,
                    useValue: {
                        dispatch: jest.fn(),
                        pipe: jest.fn(),
                        select: jest.fn()
                    }
                }
            ]
        });

        actions = TestBed.get(Actions);
        effects = TestBed.get(PipelinesEffects);
        pipelineService = TestBed.get(PipelineService);
    });

    it('should be created', () => {
        expect(effects).toBeTruthy();
    });

    describe('getEditPipelineBlueprint', () => {
        it('should return a LoadEditBlueprintsAction, with the blueprint, index 0 and empty blueprints array, on success', () => {
            const pipelineBlueprint = generatePipelineBlueprint();
            const action = new EditPipelineAction(pipelineBlueprint.ref.uuid);
            const outcome = new LoadEditBlueprintsAction(pipelineBlueprint, 0, []);

            actions.stream = hot('-a', {a: action});
            const response = cold('-a|', {a: pipelineBlueprint});
            const expected = cold('--b', {b: outcome});
            pipelineService.getPipelineBlueprint = jest.fn(() => response);

            expect(effects.getEditPipelineBlueprint$).toBeObservable(expected);

        })
    })
});
