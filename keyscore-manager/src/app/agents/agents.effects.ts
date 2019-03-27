import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {Action, Store} from "@ngrx/store";
import {Observable, of, pipe} from "rxjs";
import {exhaustMap, withLatestFrom} from "rxjs/internal/operators";
import {catchError, map, mergeMap} from "rxjs/operators";
import {AppState} from "../app.component";
import {selectAppConfig} from "../app.config";
import {
    DELETE_AGENT_SUCCESS,
    DeleteAgentFailureAction,
    DeleteAgentSuccessAction,
    InspectAgentAction,
    LOAD_AGENTS,
    LoadAgentsAction,
    LoadAgentsFailureAction,
    LoadAgentsSuccessAction,
    REMOVE_AGENT,
    RemoveCurrentAgentAction
} from "./agents.actions";
import {Go} from "../router/router.actions";
import {Agent} from "../../../modules/keyscore-manager-models/src/main/common/Agent";
import {RequestOptions} from "@angular/http";
import {KeycloakService} from "keycloak-angular";
import {AgentService} from "../services/rest-api/AgentService";

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

    @Effect() public deleteAgent$: Observable<Action> = this.actions$.pipe(
        ofType(REMOVE_AGENT),
        map(action => (action as RemoveCurrentAgentAction)),
        exhaustMap((action) => {
                return this.agentService.deleteAgent(action.id).pipe(
                    map(() => new DeleteAgentSuccessAction()),
                    catchError((cause: any) => of(new DeleteAgentFailureAction(cause)))
                );
            }
        )
    );

    @Effect() public loadAgents$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_AGENTS),
        mergeMap((_) => {
            try {
                return this.agentService.loadAgents().pipe(
                        map((data) => new LoadAgentsSuccessAction((data as Agent[]))),
                        catchError((cause: any) => of(new LoadAgentsFailureAction(cause)))
                )
            } catch (exception) {
                return of(new LoadAgentsFailureAction(exception));
            }
        })
    );

    @Effect() public redirectOnRemoveAgent$: Observable<Action> = this.actions$.pipe(
        ofType(DELETE_AGENT_SUCCESS),
        map(_ => new Go({path: ["/agent/"]}))
    );

    constructor(private store: Store<AppState>, private actions$: Actions, private agentService: AgentService) {

    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        return regEx.test(action.payload.event.url);

    }

    private getAgentIdfromRouterAction(action: RouterNavigationAction) {
        return action.payload.routerState.root.firstChild.firstChild.url[0].path;
    }
}
