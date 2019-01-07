import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {Observable} from "rxjs/index";
import {ErrorAction, ResetErrorAction} from "./error.actions";
import {map} from "rxjs/operators";
import {ROUTER_NAVIGATION, RouterNavigationAction} from "@ngrx/router-store";
import {EDIT_PIPELINE_FAILURE, EditPipelineFailureAction} from "../../pipelines/actions/pipelines.actions";

type errorTypes =
    |EditPipelineFailureAction;


const errorActions = [
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
