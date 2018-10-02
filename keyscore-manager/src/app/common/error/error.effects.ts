import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {Observable} from "rxjs/index";
import {LOAD_FILTER_CONFIGURATION_FAILURE, LoadFilterConfigurationFailure} from "../../live-editing/filters.actions";
import {ErrorAction} from "./error.actions";
import {map} from "rxjs/operators";

type errorTypes =
    |LoadFilterConfigurationFailure;


const errorActions = [
    LOAD_FILTER_CONFIGURATION_FAILURE
];

@Injectable()
export class ErrorEffects {
    @Effect()
    public handleError$: Observable<Action> = this.actions$.pipe(
        ofType<errorTypes>(...errorActions),
        map((action) => action.cause),
        map((cause) => new ErrorAction(cause.status, cause.message))
    );

    constructor(private actions$: Actions, private store: Store<any>) {

    }
}
