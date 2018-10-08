import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {Observable, of} from "rxjs/index";
import {map, mergeMap, tap} from "rxjs/internal/operators";
import {
    EDIT_PIPELINE,
    EDIT_PIPELINE_FAILURE,
    EDIT_PIPELINE_SUCCESS,
    EditPipelineAction,
    EditPipelineFailureAction,
    EditPipelineSuccessAction,
    LOAD_ALL_PIPELINES,
    LOAD_ALL_PIPELINES_FAILURE,
    LOAD_ALL_PIPELINES_SUCCESS,
    LoadAllPipelinesAction,
    LoadAllPipelinesSuccessAction,
    UpdatePipelinePollingAction
} from "../../pipelines/pipelines.actions";
import {
    DecrementLoadingCounterAction,
    HideSpinner,
    IncrementLoadingCounterAction,
    ShowSpinner,
    UPDATE_REFRESH_TIME,
    UpdateRefreshTimeAction
} from "./loading.actions";
import {
    LOAD_FILTER_CONFIGURATION,
    LOAD_FILTER_CONFIGURATION_FAILURE,
    LOAD_FILTER_CONFIGURATION_SUCCESS,
    LoadFilterConfigurationAction,
    LoadFilterConfigurationFailure,
    LoadFilterConfigurationSuccess
} from "../../live-editing/live-editing.actions";
import {
    LOAD_AGENTS,
    LOAD_AGENTS_FAILURE,
    LOAD_AGENTS_SUCCESS,
    LoadAgentsAction,
    LoadAgentsFailureAction,
    LoadAgentsSuccessAction
} from "../../agents/agents.actions";

type showSpinnerTypes =
    | EditPipelineAction
    | LoadAllPipelinesAction
    | LoadFilterConfigurationAction
    | LoadAgentsAction;

const showSpinnerActions = [
    EDIT_PIPELINE,
    LOAD_ALL_PIPELINES,
    LOAD_FILTER_CONFIGURATION,
    LOAD_AGENTS
];

type hideSpinnerTypes =
    | LoadAllPipelinesSuccessAction
    | EditPipelineSuccessAction
    | EditPipelineFailureAction
    | LoadFilterConfigurationSuccess
    | LoadFilterConfigurationFailure
    | LoadAgentsFailureAction
    | LoadAgentsSuccessAction;

const hideSpinnerActions = [
    LOAD_ALL_PIPELINES_SUCCESS,
    LOAD_ALL_PIPELINES_FAILURE,
    EDIT_PIPELINE_SUCCESS,
    EDIT_PIPELINE_FAILURE,
    LOAD_FILTER_CONFIGURATION_SUCCESS,
    LOAD_FILTER_CONFIGURATION_FAILURE,
    LOAD_AGENTS_FAILURE,
    LOAD_AGENTS_SUCCESS
];

@Injectable()
export class LoadingEffects {
    @Effect()
    public showSpinner$: Observable<Action> = this.actions$.pipe(
        ofType<showSpinnerTypes>(...showSpinnerActions),
        tap((_) => this.store.dispatch(new IncrementLoadingCounterAction())),
        map(() => new ShowSpinner())
    );

    @Effect()
    public hideSpinner$: Observable<Action> = this.actions$.pipe(
        ofType<hideSpinnerTypes>(...hideSpinnerActions),
        tap((_) => this.store.dispatch(new DecrementLoadingCounterAction())),
        map(() => new HideSpinner())
    );

    @Effect()
    public handleRefreshTimeUpdate$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_REFRESH_TIME),
        map((action) =>
            [(action as UpdateRefreshTimeAction).newRefreshTime, (action as UpdateRefreshTimeAction).oldRefreshTime]),
        tap(([newRefreshTime, oldRefreshTime]) => {
            if (newRefreshTime < 0) {
                this.store.dispatch(new UpdatePipelinePollingAction(false));
            }
            if (newRefreshTime > 0 && oldRefreshTime < 0) {
                this.store.dispatch(new UpdatePipelinePollingAction(true));
                this.store.dispatch(new LoadAllPipelinesAction());
            }
        }),
        mergeMap((_) => {
            return of();
        })
    );

    constructor(private actions$: Actions, private store: Store<any>) {

    }
}
