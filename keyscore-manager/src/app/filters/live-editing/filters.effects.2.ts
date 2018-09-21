import {Injectable} from "@angular/core";
import {AppState} from "../../app.component";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Observable, of} from "rxjs/index";
import {ROUTER_NAVIGATION, RouterNavigationAction} from "@ngrx/router-store";
import {mergeMap} from "rxjs/internal/operators";
import {Action, Store} from "@ngrx/store";
import {InitializeLiveEditingDataAction} from "./filters.actions";
import {FilterService} from "../../services/rest-api/filter.service";


@Injectable()
export class FiltersEffects2 {

    @Effect()
    public initializeLiveEditing$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
            const regex =  /\/filter\/.*/g;
            if(this.handleNavigation(regex, action as RouterNavigationAction)) {

            }

            const navigationAction = action as RouterNavigationAction;
            const url = navigationAction.payload.event.url;
            const filterId = url.substring(url.lastIndexOf("/") + 1, url.length);
            const filterIdRegex =
                /\/filter\/.*/g;
            if (filterIdRegex.test(url)) {
                return of(new InitializeLiveEditingDataAction(filterId));
            } else {
                return of();
            }
        })
    );


    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private filerService: FilterService)
                {
    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        console.log("URL: ", action.payload.event.url);
        return regEx.test(action.payload.event.url);
    }
}