import {switchMap} from "rxjs/operators";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action} from "@ngrx/store";
import {of} from "rxjs/observable/of";
import {Observable} from "rxjs/Rx";
import {ROUTER_NAVIGATION} from '@ngrx/router-store'
import {Injectable} from "@angular/core";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {EditStreamAction} from "./streams.actions";

@Injectable()
export class StreamsEffects {
    @Effect() editStream$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        switchMap(action => {
            const navigationAction = action as RouterNavigationAction;
            const url = navigationAction.payload.event.url;
            if (url.startsWith('/stream')) {
                const id = url.substring(url.indexOf('/stream/') + 8);
                return of(new EditStreamAction(id))
            }
            return of({type: 'NOOP'})
        })
    );

    constructor(private actions$: Actions) {
    }
}