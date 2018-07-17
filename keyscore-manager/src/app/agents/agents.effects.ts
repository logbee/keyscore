import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {Action, Store} from "@ngrx/store";
import {Observable, of} from "rxjs";
import {withLatestFrom} from "rxjs/internal/operators";
import {catchError, map, mergeMap} from "rxjs/operators";
import {AppState} from "../app.component";
import {selectAppConfig} from "../app.config";
import {
    InspectAgentAction,
    LOAD_AGENTS,
    LoadAgentsAction,
    LoadAgentsFailureAction,
    LoadAgentsSuccessAction
} from "./agents.actions";
import {AgentModel} from "./agents.model";

@Injectable()
export class AgentsEffects {
    @Effect() public triggerLoadAgentsOnNavigation$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
            const regex = /\/agent.*/g;
            if (this.handleNavigation(regex, action as RouterNavigationAction)) {
                return of(new LoadAgentsAction());
            }
            return of();
        })
    );

    @Effect() public removeAgentsOnNavigation$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
                const agentWithId = /\/agent\/.+/g;
                if (this.handleNavigation(agentWithId, action as RouterNavigationAction)) {
                    return of(new InspectAgentAction(
                        this.getAgentIdfromRouterAction(action as RouterNavigationAction)
                    ));
                }
                return of();
            }
        ));

    @Effect() public loadAgents$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_AGENTS),
        withLatestFrom(this.store.select(selectAppConfig)),
        mergeMap(([action, appConfig]) => {
            try {
                const url: string = appConfig.getString("keyscore.frontier.base-url") + "/agents/";
                return this.http.get(url).pipe(
                    map((data) => new LoadAgentsSuccessAction((data as AgentModel[]))),
                    catchError((cause: any) => of(new LoadAgentsFailureAction(cause)))
                );
            } catch (exception) {
                return of(new LoadAgentsFailureAction(exception));
            }
        })
    );

    constructor(private store: Store<AppState>, private actions$: Actions, private http: HttpClient) {

    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        return regEx.test(action.payload.event.url);

    }

    private getAgentIdfromRouterAction(action: RouterNavigationAction) {
        return action.payload.routerState.root.firstChild.firstChild.url[0].path;
    }
}
