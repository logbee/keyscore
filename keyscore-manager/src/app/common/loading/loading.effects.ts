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

type showSpinnerTypes =
    | EditPipelineAction
    | LoadAllPipelinesAction;

const showSpinnerActions = [
    EDIT_PIPELINE,
    LOAD_ALL_PIPELINES
];

type hideSpinnerTypes =
    | LoadAllPipelinesSuccessAction
    | EditPipelineSuccessAction
    | EditPipelineFailureAction;

const hideSpinnerActions = [
    LOAD_ALL_PIPELINES_SUCCESS,
    EDIT_PIPELINE_SUCCESS,
    EDIT_PIPELINE_FAILURE
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
