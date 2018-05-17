import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Observable, of} from "rxjs/index";
import {Action, Store} from "@ngrx/store";
import {LoadFilterModelFromStreamAction} from "./filters.actions";
import {map} from "rxjs/operators";
import {AppState} from "../../app.component";
import {HttpClient} from "@angular/common/http";
import {FilterModel, getFilterById} from "../streams.model";
import {TranslateService} from "@ngx-translate/core";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {switchMap} from "rxjs/internal/operators";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";

@Injectable()
export class FilterEffects {

    @Effect()
    navigateToLiveEditing$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        map( (action: RouterNavigationAction) => {
            console.log("reached navigateToLiveEditing Effect");
            const navigationAction = action as RouterNavigationAction;
            const url = navigationAction.payload.event.url;
            const currentFilterId = url.substr(url.lastIndexOf("/")+1, url.length);
            const filterWithId = /\/filter\/.*/g;
            if (filterWithId.test(url)) {
                return this.store.select(getFilterById(currentFilterId));
            }
            return of(undefined)
        }),
        switchMap((filterOrUndef$) =>{
            if (filterOrUndef$) {
                return of(new LoadFilterModelFromStreamAction(filterOrUndef$))
            }

            return of({type: 'NOOP'});
        })
    );

    constructor(private store: Store<AppState>, private actions$: Actions, private http: HttpClient, private translate: TranslateService) {

    }
}
