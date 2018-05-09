import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Observable, of} from "rxjs/index";
import {Action, Store} from "@ngrx/store";
import {
    GET_CURRENT_DESCRIPTOR,
    GetCurrentDescriptorAction, GetCurrentDescriptorFailureAction,
    SET_CURRENT_FILTER,
    SetCurrentFilterAction
} from "./filters.actions";
import {switchMap} from "rxjs/internal/operators";
import {selectAppConfig} from "../app.config";
import {catchError, combineLatest, map, mergeMap} from "rxjs/operators";
import {AppState} from "../app.component";
import {HttpClient} from "@angular/common/http";
import {
    LoadFilterDescriptorsFailureAction,
    LoadFilterDescriptorsSuccessAction,
    UpdateStreamAction
} from "../streams/streams.actions";
import {FilterDescriptor} from "../streams/streams.model";
import {TranslateService} from "@ngx-translate/core";

@Injectable()
export class FilterEffects {

    @Effect() getFilterDescriptorByName$: Observable<Action> = this.actions$.pipe(
        ofType(GET_CURRENT_DESCRIPTOR),
        map(action => (action as GetCurrentDescriptorAction).filterName),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([filterName, config]) =>
            this.http.get(config.getString('keyscore.frontier.base-url') + '/descriptors?language=' + this.translate.currentLang).pipe(
                map((data: FilterDescriptor[]) => {
                    let currentDescriptor = data.find(descriptor => descriptor.name === filterName);
                    return new SetCurrentFilterAction(currentDescriptor)
                }),
                catchError(cause => of(new GetCurrentDescriptorFailureAction(cause)))
            )
        )
    );

    constructor(private store: Store<AppState>, private actions$: Actions, private http: HttpClient, private translate: TranslateService) {

    }
}

