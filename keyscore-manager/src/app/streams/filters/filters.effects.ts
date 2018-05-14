import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Observable, of} from "rxjs/index";
import {Action, select, Store} from "@ngrx/store";
import {
    LOAD_FILTER_DESCRIPTOR,
    LoadFilterDescriptorAction,
    LoadFilterDescriptorFailureAction,
    LoadFilterDescriptorSuccessAction
} from "./filters.actions";
import {selectAppConfig} from "../../app.config";
import {catchError, combineLatest, map, mergeMap} from "rxjs/operators";
import {AppState} from "../../app.component";
import {HttpClient} from "@angular/common/http";
import {FilterDescriptor, FilterModel, getFilterById, getStreamsModuleState} from "../streams.model";
import {TranslateService} from "@ngx-translate/core";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {switchMap} from "rxjs/internal/operators";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";

@Injectable()
export class FilterEffects {

    @Effect() getFilterDescriptorByName$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_DESCRIPTOR),
        map(action => (action as LoadFilterDescriptorAction).filterName),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([filterName, config]) =>
            this.http.get(config.getString('keyscore.frontier.base-url') + '/descriptors?language=' + this.translate.currentLang).pipe(
                map((data: FilterDescriptor[]) => {
                    let currentDescriptor = data.find(descriptor => descriptor.name === filterName);
                    console.log(currentDescriptor.name);
                    return new LoadFilterDescriptorSuccessAction(currentDescriptor)
                }),
                catchError(cause => of(new LoadFilterDescriptorFailureAction(cause)))
            )
        )
    );


    @Effect()
    navigateToLiveEditing$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        map( (action: RouterNavigationAction) => {

            const navigationAction = action as RouterNavigationAction;
            const url = navigationAction.payload.event.url;
            const filterWithId = /\/filter\/.*/g;

            if (filterWithId.test(url)) {
                return this.store.select(getFilterById("<uuid>"));
            }

            return of(undefined)
        }),
        switchMap(filterOrUndef =>{

            if (filterOrUndef) {
                // return of(new LiveEditFilterAction(filterOrUndef))
            }

            return of({type: 'NOOP'});
        })
    );

    constructor(private store: Store<AppState>, private actions$: Actions, private http: HttpClient, private translate: TranslateService) {

    }
}
