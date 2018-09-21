import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {Observable, of} from "rxjs/index";
import {AppState} from "../../app.component";
import {HttpClient} from "@angular/common/http";
import {FilterService} from "../../services/rest-api/filter.service";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {mergeMap} from "rxjs/internal/operators";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {catchError, map} from "rxjs/operators";
import {Blueprint} from "../../models/blueprints/Blueprint";
import {LoadAllBlueprintsActionsFailure, LoadAllBlueprintsActionsSuccess} from "./resource-viewer.actions";

@Injectable()
export class ResourceViewerEffects {
    @Effect()
    public initializing$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
            const regex = /\/resources/g;
            if (this.handleNavigation(regex, action as RouterNavigationAction)) {
                return this.filterService.loadAllBlueprints().pipe(
                    map((data: Blueprint[]) =>
                        new LoadAllBlueprintsActionsSuccess(data)),
                    catchError((cause: any) => of(new LoadAllBlueprintsActionsFailure(cause)))
                )
            }
            return of();
        })
    );

    constructor(private store: Store<AppState>,
                private httpClient: HttpClient,
                private actions$: Actions,
                private filterService: FilterService) {
    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        console.log("URL: ", action.payload.event.url);
        return regEx.test(action.payload.event.url);

    }
}