import {Injectable} from "@angular/core";
import {catchError, combineLatest, map, switchMap} from "rxjs/operators";
import {Action, State, Store} from "@ngrx/store";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {of} from "rxjs";
import {Observable} from "rxjs";
import {AppState} from "../app.component";
import {HttpClient} from "@angular/common/http";
import {
    InspectAgentAction,
    LOAD_AGENTS,
    LoadAgentsAction,
    LoadAgentsFailureAction,
    LoadAgentsSuccessAction,
    RemoveCurrentAgentAction
} from "./agents.actions";
import {selectAppConfig} from "../app.config";
import {AgentModel} from "./agents.model";
import {ActivatedRouteSnapshot} from "@angular/router";

@Injectable()
export class AgentsEffects {
    @Effect() triggerLoadAgentsOnNavigation$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        switchMap(action => {
            const regex = /\/agent.*/g;
            if (this.handleNavigation(regex,action as RouterNavigationAction)) {
                return of(new LoadAgentsAction());
            }
            return of();
        })
    );

    @Effect() removeAgentsOnNavigation$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        switchMap(action => {
                const agentWithId = /\/agent\/.+/g;
                if (this.handleNavigation(agentWithId,action as RouterNavigationAction)) {
                    return of(new InspectAgentAction(this.getAgentIdfromRouterAction(action as RouterNavigationAction)));
                }
                return of();
            }
        ));

    @Effect() loadAgents$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_AGENTS),
        combineLatest(this.store.select(selectAppConfig)),
        switchMap(([action, appConfig]) => {
            try {
                const url: string = appConfig.getString('keyscore.frontier.base-url') + '/agents/';
                return this.http.get(url).pipe(
                    map(data => new LoadAgentsSuccessAction((data as AgentModel[]))),
                    catchError((cause: any) => of(new LoadAgentsFailureAction(cause)))
                );
            }
            catch (exception) {
                return of(new LoadAgentsFailureAction(exception));
            }
        })
    );


    constructor(private store: Store<AppState>, private actions$: Actions, private http: HttpClient) {

    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        return regEx.test(action.payload.event.url)

    }

    private getAgentIdfromRouterAction(action:RouterNavigationAction){
        return action.payload.routerState.root.firstChild.firstChild.url[0].path
    }
}