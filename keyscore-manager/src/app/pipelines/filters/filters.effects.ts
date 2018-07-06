import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {Action, Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable, of} from "rxjs/index";
import {mergeMap} from "rxjs/internal/operators";
import {AppState} from "../../app.component";
import {ConfigureFilterAction} from "./filters.actions";

@Injectable()
export class FilterEffects {

    @Effect()
    public navigateToLiveEditing$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action: RouterNavigationAction) => {
            const navigationAction = action as RouterNavigationAction;
            const url = navigationAction.payload.event.url;
            const currentFilterId = url.substr(url.lastIndexOf("/") + 1, url.length);
            const filterWithId = /\/filter\/.*/g;
            if (filterWithId.test(url)) {
                return of(new ConfigureFilterAction(currentFilterId));
            }
            return of();
        }));

    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private http: HttpClient,
                private translate: TranslateService) {
    }
}
