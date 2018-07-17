import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {Action, Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable, of} from "rxjs/index";
import {catchError, concatMap, map, mergeMap, switchMap} from "rxjs/internal/operators";
import {AppState} from "../app.component";
import {
    DRAIN_FILTER,
    DrainFilterAction,
    DrainFilterFailure,
    DrainFilterSuccess,
    EXTRACT_DATASETS,
    ExtractDatasetsAction,
    ExtractDatasetsFailure,
    ExtractDatasetsSuccess, InitalizeLiveEditingDataAction, INITIALIZE_LIVE_EDITING_DATA,
    INSERT_DATASETS,
    InsertDatasetsAction,
    InsertDatasetsFailure,
    InsertDatasetsSuccess,
    LOAD_FILTERSTATE,
    LOAD_LIVE_EDITING_FILTER, LOAD_LIVE_EDITING_FILTER_FAILURE, LOAD_LIVE_EDITING_FILTER_SUCCESS,
    LoadFilterStateAction,
    LoadFilterStateFailure,
    LoadFilterStateSuccess,
    LoadLiveEditingFilterAction,
    LoadLiveEditingFilterFailure,
    LoadLiveEditingFilterSuccess,
    PAUSE_FILTER,
    PauseFilterAction,
    PauseFilterFailure,
    PauseFilterSuccess
} from "./filters.actions";
import {combineLatest} from "rxjs/operators";
import {selectAppConfig} from "../app.config";
import {FilterConfiguration} from "../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../models/filter-model/FilterInstanceState";
@Injectable()
export class FilterEffects {
    @Effect()
    public initalizeLiveEditingData$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
                const navigationAction = action as RouterNavigationAction;
                const url = navigationAction.payload.event.url;
                const filterId = url.substring(url.lastIndexOf("/") + 1, url.length);
                const filterIdRegex =
                    /\/filter\/.*/g;
                if (filterIdRegex.test(url)) {
                    return of(new InitalizeLiveEditingDataAction(filterId));
                } else {
                    return of();
                }
            }
        )
    );
    @Effect()
    public navigateToLiveEditing$: Observable<Action> = this.actions$.pipe(
        ofType(INITIALIZE_LIVE_EDITING_DATA),
        map((action) => (action as InitalizeLiveEditingDataAction)),
        concatMap((payload) => [
            new PauseFilterAction(payload.filterId, true),
            new DrainFilterAction(payload.filterId, true),
            new LoadLiveEditingFilterAction(payload.filterId)]
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
                map((data: FilterConfiguration) => new LoadLiveEditingFilterSuccess(data, action.filterId)),
                catchError((cause: any) => of(new LoadLiveEditingFilterFailure(cause)))
            );
        })
    );
    @Effect()
    public loadFilterState$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTERSTATE),
        map((action) => (action as LoadFilterStateAction)),
        combineLatest(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.get(appconfig.getString("keyscore.frontier.base-url") +
                "/filter/" + action.filterId + "/state").pipe(
                map((state: FilterInstanceState) => new LoadFilterStateSuccess(state)),
                catchError((cause: any) => of(new LoadFilterStateFailure(cause))));
        })
    );
    @Effect()
    public pauseFilter$: Observable<Action> = this.actions$.pipe(
        ofType(PAUSE_FILTER),
        map((action) => (action as PauseFilterAction)),
        combineLatest(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.post(appconfig.getString("keyscore.frontier.base-url") + "/filter/" +
                action.filterId + "/pause?value=" + action.pause, {}, {
                headers: new HttpHeaders().set("Content-Type", "application/json"),
                responseType: "json"
            }).pipe(
                map((state: FilterInstanceState) => new PauseFilterSuccess(state)),
                catchError((cause: any) => of(new PauseFilterFailure(cause)))
            );
        })
    );
    @Effect()
    public drainFilter$: Observable<Action> = this.actions$.pipe(
        ofType(DRAIN_FILTER),
        map((action) => (action as DrainFilterAction)),
        combineLatest(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.post(appconfig.getString("keyscore.frontier.base-url") + "/filter/" + action.filterId +
                "/drain?value=" + action.drain, {}, {
                headers: new HttpHeaders().set("Content-Type", "application/json"),
                responseType: "json"
            }).pipe(
                map((state: FilterInstanceState) => new DrainFilterSuccess(state)),
                catchError((cause: any) => of(new DrainFilterFailure(cause)))
            );
        })
    );
    @Effect()
    public insertDatasets: Observable<Action> = this.actions$.pipe(
        ofType(INSERT_DATASETS),
        map((action) => (action as InsertDatasetsAction)),
        combineLatest(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.put(appconfig + "/filter/" + action.filterId + "/insert", action.datasets, {
                headers: new HttpHeaders().set("Content-Type", "application/json"),
                responseType: "json"
            }).pipe(
                map((state: FilterInstanceState) => new InsertDatasetsSuccess(state)),
                catchError((cause: any) => of(new InsertDatasetsFailure(cause)))
            );
        }),
    );
    @Effect()
    public extractDatasets: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_LIVE_EDITING_FILTER_SUCCESS),
        map((action) => (action as LoadLiveEditingFilterSuccess)),
        combineLatest(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.put(appconfig.getString("keyscore.frontier.base-url") +
                "/filter/" + action.filterId + "/extract?amount=10", {}, {
                headers: new HttpHeaders().set("Content-Type", "application/json"),
                responseType: "json"
            }).pipe(
                map((datasets: any) => new ExtractDatasetsSuccess(datasets)),
                catchError((cause: any) => of(new ExtractDatasetsFailure(cause)))
            );
        }),
    );

    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private http: HttpClient,
                private translate: TranslateService) {
    }

}
