import {
    EDIT_PIPELINE, EDIT_PIPELINE_FAILURE, EDIT_PIPELINE_SUCCESS, EditPipelineAction, EditPipelineFailureAction,
    EditPipelineSuccessAction,
    LOAD_ALL_PIPELINES,
    LOAD_ALL_PIPELINES_SUCCESS,
    LoadAllPipelinesAction, LoadAllPipelinesSuccessAction
} from "../pipelines/pipelines.actions";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Observable, pipe} from "rxjs/index";
import {Action} from "@ngrx/store";
import {map} from "rxjs/internal/operators";
import {HideSpinner, ShowSpinner} from "./loading.actions";

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

    constructor(private actions$:Actions){

    }
}