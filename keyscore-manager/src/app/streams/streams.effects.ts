import {catchError, combineLatest, map, switchMap} from "rxjs/operators";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {of} from "rxjs/observable/of";
import {Observable} from "rxjs/Rx";
import {ROUTER_NAVIGATION} from '@ngrx/router-store'
import {Injectable} from "@angular/core";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {
    EditStreamAction,
    UPDATE_STREAM,
    UpdateStreamAction,
    UpdateStreamFailureAction,
    UpdateStreamSuccessAction
} from "./streams.actions";
import {HttpClient} from "@angular/common/http";
import {AppState} from "../app.component";
import {selectAppConfig} from "../app.config";

@Injectable()
export class StreamsEffects {
    @Effect() editStream$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        switchMap(action => {
            const navigationAction = action as RouterNavigationAction;
            const url = navigationAction.payload.event.url;
            if (url.startsWith('/stream')) {
                const id = url.substring(url.indexOf('/stream/') + 8);
                return of(new EditStreamAction(id));
            }
            return of({type: 'NOOP'});
        })
    );

    @Effect() updateStream$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_STREAM),
        map(action => (action as UpdateStreamAction).stream),
        combineLatest(this.store.select(selectAppConfig)),
        switchMap(([stream, config]) => {
            const url: string = config.getString('keyscore.frontier.base-url');
            return this.http.get('url').pipe(
                map(data => new UpdateStreamSuccessAction(stream)),
                catchError((cause: any) => of(new UpdateStreamFailureAction(stream)))
            );
        })
    );

    constructor(private store: Store<AppState>, private actions$: Actions, private http: HttpClient) {
    }
}