import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {Observable} from "rxjs/index";
import {LOAD_FILTER_CONFIGURATION_FAILURE, LoadFilterConfigurationFailure} from "../../live-editing/live-editing.actions";
import {ErrorAction, ResetErrorAction} from "./error.actions";
import {map} from "rxjs/operators";
import {tap} from "rxjs/internal/operators";
import {ROUTER_NAVIGATION, RouterNavigationAction} from "@ngrx/router-store";
import {EDIT_PIPELINE_FAILURE, EditPipelineFailureAction} from "../../pipelines/actions/pipelines.actions";

type errorTypes =
    |LoadFilterConfigurationFailure
    |EditPipelineFailureAction;


const errorActions = [
    LOAD_FILTER_CONFIGURATION_FAILURE,
    EDIT_PIPELINE_FAILURE
];

type errorResetTypes =
    |RouterNavigationAction;

const errorResetActions = [
    ROUTER_NAVIGATION
];

@Injectable()
export class ErrorEffects {
    @Effect()
    public handleError$: Observable<Action> = this.actions$.pipe(
        ofType<errorTypes>(...errorActions),
        map((action) => action.cause),
        map((cause) => new ErrorAction(cause.status, cause.message))
    );

    @Effect()
    public resetError$: Observable<Action> = this.actions$.pipe(
        ofType<errorResetTypes>(...errorResetActions),
        map(_ => new ResetErrorAction())
    );

    constructor(private actions$: Actions, private store: Store<any>) {

    }
}
