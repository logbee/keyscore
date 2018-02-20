import {catchError, combineLatest, map, mergeMap, switchMap} from "rxjs/operators";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {of} from "rxjs/observable/of";
import {Observable} from "rxjs/Rx";
import {ROUTER_NAVIGATION} from '@ngrx/router-store'
import {Injectable} from "@angular/core";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {
    EditStreamAction,
    LOAD_FILTER_DESCRIPTORS,
    LoadFilterDescriptorsFailureAction,
    LoadFilterDescriptorsSuccessAction,
    UPDATE_STREAM,
    UpdateStreamAction,
    UpdateStreamFailureAction,
    UpdateStreamSuccessAction
} from "./streams.actions";
import {HttpClient} from "@angular/common/http";
import {AppState} from "../app.component";
import {selectAppConfig} from "../app.config";
import {FilterDescriptor} from "./streams.model";

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
            const streamUrl: string = config.getString('keyscore.frontier.base-url')+'/stream/';
            return this.http.put(streamUrl+stream.id,{}).pipe(
                map(data => new UpdateStreamSuccessAction(stream)),
                catchError((cause: any) => of(new UpdateStreamFailureAction(stream)))
            );
        })
    );

    @Effect() loadFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_DESCRIPTORS),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([action, config]) =>
            this.http.get(config.getString('keyscore.frontier.base-url') + '/descriptors').pipe(
                map((data: FilterDescriptor[]) => new LoadFilterDescriptorsSuccessAction(data)),
                catchError(cause => of(new LoadFilterDescriptorsFailureAction(cause)))
            )
        )
    );

    constructor(private store: Store<AppState>, private actions$: Actions, private http: HttpClient) {
    }
}