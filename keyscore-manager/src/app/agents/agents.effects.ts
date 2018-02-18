import {Injectable} from "@angular/core";
import {catchError, combineLatest, map, switchMap} from "rxjs/operators";
import {Action, Store} from "@ngrx/store";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {of} from "rxjs/observable/of";
import {Observable} from "rxjs/Rx";
import {AppState} from "../app.component";
import {HttpClient} from "@angular/common/http";
import {LOAD_AGENTS, LoadAgentsAction, LoadAgentsFailureAction, LoadAgentsSuccessAction} from "./agents.actions";
import {selectAppConfig} from "../app.config";
import {AgentModel} from "./agents.model";

@Injectable()
export class AgentsEffects {
    @Effect() triggerLoadAgentsOnNavigation$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        switchMap(action => {
            const navigationAction = action as RouterNavigationAction;
            const url = navigationAction.payload.event.url;
            if (url.startsWith('/agent')) {
                return of(new LoadAgentsAction());
            }
            return of({type: 'NOOP'});
        })
    );

    @Effect() loadAgents$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_AGENTS),
        combineLatest(this.store.select(selectAppConfig)),
        switchMap(([action, config]) => {
            const url: string = config.getString('keyscore.frontier.base-url')+'/agent/';
            return this.http.get(url).pipe(
                map(data => new LoadAgentsSuccessAction((data as AgentModel[]))),
                catchError((cause: any) => of(new LoadAgentsFailureAction()))
            );
        })
    );

    constructor(private store: Store<AppState>, private actions$: Actions, private http: HttpClient) {

    }
}