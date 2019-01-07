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
    LOAD_ALL_PIPELINE_INSTANCES,
    LOAD_ALL_PIPELINE_INSTANCES_FAILURE,
    LOAD_ALL_PIPELINE_INSTANCES_SUCCESS,
    LOAD_PIPELINEBLUEPRINTS,
    LOAD_PIPELINEBLUEPRINTS_FAILURE,
    LoadAllPipelineInstancesAction,
    LoadAllPipelineInstancesSuccessAction,
    LoadPipelineBlueprints,
    RUN_PIPELINE, RUN_PIPELINE_FAILURE, RUN_PIPELINE_SUCCESS,
    RunPipelineAction, RunPipelineFailureAction,
    RunPipelineSuccessAction,
    UpdatePipelinePollingAction
} from "../../pipelines/actions/pipelines.actions";
import {
    DecrementLoadingCounterAction,
    HideSpinner,
    IncrementLoadingCounterAction,
    ShowSpinner,
    UPDATE_REFRESH_TIME,
    UpdateRefreshTimeAction
} from "./loading.actions";

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
    | LoadAgentsAction
    | RunPipelineAction
    | LoadPipelineBlueprints

const showSpinnerActions = [
    EDIT_PIPELINE,
    LOAD_PIPELINEBLUEPRINTS,
    LOAD_ALL_PIPELINE_INSTANCES,
    LOAD_AGENTS,
    RUN_PIPELINE
];

type hideSpinnerTypes =
    | LoadAllPipelineInstancesSuccessAction
    | EditPipelineSuccessAction
    | EditPipelineFailureAction
    | LoadAgentsFailureAction
    | RunPipelineSuccessAction
    | RunPipelineFailureAction
    | LoadAgentsSuccessAction;

const hideSpinnerActions = [
    LOAD_ALL_PIPELINE_INSTANCES_SUCCESS,
    LOAD_ALL_PIPELINE_INSTANCES_FAILURE,
    LOAD_PIPELINEBLUEPRINTS_FAILURE,
    EDIT_PIPELINE_SUCCESS,
    EDIT_PIPELINE_FAILURE,
    LOAD_AGENTS_FAILURE,
    LOAD_AGENTS_SUCCESS,
    RUN_PIPELINE_SUCCESS,
    RUN_PIPELINE_FAILURE,
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
                this.store.dispatch(new LoadAllPipelineInstancesAction());
            }
        }),
        mergeMap((_) => {
            return of();
        })
    );

    constructor(private actions$: Actions, private store: Store<any>) {

    }
}
