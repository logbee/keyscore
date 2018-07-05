import {
    EDIT_PIPELINE, EDIT_PIPELINE_FAILURE, EDIT_PIPELINE_SUCCESS, EditPipelineAction, EditPipelineFailureAction,
    EditPipelineSuccessAction,
    LOAD_ALL_PIPELINES,
    LOAD_ALL_PIPELINES_SUCCESS,
    LoadAllPipelinesAction, LoadAllPipelinesSuccessAction, UpdatePipelinePollingAction
} from "../pipelines/pipelines.actions";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Observable, of, pipe} from "rxjs/index";
import {Action, Store} from "@ngrx/store";
import {map, mergeMap, tap} from "rxjs/internal/operators";
import {HideSpinner, ShowSpinner, UPDATE_REFRESH_TIME, UpdateRefreshTimeAction} from "./loading.actions";

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
export class LoadingEffects{
    @Effect()
    showSpinner$:Observable<Action> = this.actions$.pipe(
        ofType<showSpinnerTypes>(...showSpinnerActions),
        map(()=> new ShowSpinner())
    );

    @Effect()
    hideSpinner$:Observable<Action> = this.actions$.pipe(
        ofType<hideSpinnerTypes>(...hideSpinnerActions),
        map(()=> new HideSpinner())
    );

    @Effect()
    handleRefreshTimeUpdate$:Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_REFRESH_TIME),
        map(action => [(action as UpdateRefreshTimeAction).newRefreshTime,(action as UpdateRefreshTimeAction).oldRefreshTime]),
        tap(([newRefreshTime,oldRefreshTime]) => {
            if(newRefreshTime < 0){
                this.store.dispatch(new UpdatePipelinePollingAction(false));
            }
            if (newRefreshTime > 0 && oldRefreshTime < 0) {
                this.store.dispatch(new UpdatePipelinePollingAction(true));
                this.store.dispatch(new LoadAllPipelinesAction());
            }
        }),
        mergeMap(_ => {
            return of()
        })
    );

    constructor(private actions$:Actions, private store:Store<any>){

    }
}