import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {Observable} from "rxjs/index";
import {LOAD_LIVE_EDITING_FILTER_FAILURE, LoadLiveEditingFilterFailure} from "../../pipelines/filters/filters.actions";
import {ErrorEvent} from "./error.actions";
import {map} from "rxjs/operators";


type errorTypes =
    |LoadLiveEditingFilterFailure

const errorActions = [
    LOAD_LIVE_EDITING_FILTER_FAILURE
];
@Injectable()
export class ErrorEffects {
    @Effect()
    public handleError$: Observable<Action> = this.actions$.pipe(
        ofType<errorTypes>(...errorActions),
        //TODO: get cause object and set httpError and message param properly
        map(() => new ErrorEvent("Test", "Test"))
    );

    constructor(private actions$: Actions, private store: Store<any>) {

    }
}
