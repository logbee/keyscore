import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {Action, Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable, of} from "rxjs/index";
import {catchError, map, mergeMap, switchMap} from "rxjs/internal/operators";
import {AppState} from "../../app.component";
import { ActivatedRoute, Router } from "@angular/router";
import {
    LOAD_LIVE_EDITING_FILTER,
    LoadLiveEditingFilterAction,
    LoadLiveEditingFilterSuccess,
    LoadLiveEditingFilterFailure, LOAD_LIVE_EDITING_FILTER_FAILURE
} from "./filters.actions";
import {FilterConfiguration} from "../pipelines.model";
import {combineLatest} from "rxjs/operators";
import {selectAppConfig} from "../../app.config";
import {Go} from "../../router/router.actions";

@Injectable()
export class FilterEffects {

    @Effect()
    public navigateToLiveEditing$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
                const navigationAction = action as RouterNavigationAction;
                const url = navigationAction.payload.event.url;
                const filterId = url.substring(url.lastIndexOf("/") + 1, url.length);
                const filterIdRegex =
                    /\/pipelines\/filter\/.*/g;
                if (filterIdRegex.test(url)) {
                    return of(new LoadLiveEditingFilterAction(filterId));
                } else {
                    return of();
                }
            }
        )
    );
    @Effect()
    public loadFilterConfiguration$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_LIVE_EDITING_FILTER),
        map((action) => (action as LoadLiveEditingFilterAction)),
        combineLatest(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.get(appconfig.getString("keyscore.frontier.base-url") +
                "/filter/" + action.filterId + "/config").pipe(
                map((data: FilterConfiguration) => new LoadLiveEditingFilterSuccess(data)),
                catchError((cause: any) => of(new LoadLiveEditingFilterFailure(cause)))
            );
        })
    );
    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private http: HttpClient,
                private translate: TranslateService) {
    }

}
